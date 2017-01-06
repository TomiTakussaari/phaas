package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationRepository;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import com.github.tomitakussaari.phaas.user.dao.UserRepository;
import com.github.tomitakussaari.phaas.util.CryptoHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
public class UsersService implements UserDetailsService {

    public static final String ADMIN_ROLE_VALUE = "ROLE_ADMIN";
    public static final String USER_ROLE_VALUE = "ROLE_USER";

    private final UserConfigurationRepository userConfigurationRepository;
    private final UserRepository userRepository;
    private final CryptoHelper cryptoHelper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .map(userDTO -> new PhaasUser(userDTO, userConfigurationRepository.findByUser(username), cryptoHelper))
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));
    }

    public List<PhaasUser> findAll() {
        List<PhaasUser> details = new ArrayList<>();
        for (UserDTO user : userRepository.findAll()) {
            details.add(new PhaasUser(user, userConfigurationRepository.findByUser(user.getUserName()), cryptoHelper));
        }
        return details;
    }

    boolean hasUsers() {
        return userRepository.count() > 0L;
    }

    public String createUser(String userName, PasswordEncodingAlgorithm algorithm, List<ROLE> roles, Optional<String> sharedSecretForSigningCommunication) {
        return createUser(userName, algorithm, roles, generatePassword(), sharedSecretForSigningCommunication.orElse(null));
    }

    public String createUser(String userName, PasswordEncodingAlgorithm algorithm, List<ROLE> roles, String userPassword, String sharedSecretForSigningCommunication) {
        UserDTO userDTO = new UserDTO(null, userName, passwordEncoder().encode(userPassword),
                roles.stream().map(ROLE::getValue).collect(joining(",")),
                sharedSecretForSigningCommunication);

        UserConfigurationDTO configurationDTO = createUserConfigurationDTO(algorithm, userPassword, generateEncryptionKey(), userDTO);
        save(userDTO, configurationDTO);
        return userPassword;
    }

    @Transactional
    public void renewEncryptionKeyProtection(String userName, Optional<CharSequence> newPassword, CharSequence oldPassword, Optional<String> sharedSecretForSigningCommunication) {
        userRepository.findByUserName(userName).ifPresent(user -> {
            CharSequence userPassword = newPassword.orElse(oldPassword);
            user.setPasswordHash(passwordEncoder().encode(userPassword));
            sharedSecretForSigningCommunication.ifPresent(user::setSharedSecretForSigningCommunication);

            List<UserConfigurationDTO> newConfigs = userConfigurationRepository.findByUser(user.getUserName()).stream()
                    .map(currentConfig -> {
                        String protectionKey = currentConfig.toProtectionScheme(cryptoHelper).cryptoData(oldPassword).getDataProtectionKey();
                        return createUserConfigurationDTO(currentConfig.getAlgorithm(), userPassword, protectionKey, user).setId(currentConfig.getId());
                    }).collect(Collectors.toList());

            save(user, newConfigs.toArray(new UserConfigurationDTO[]{}));
            log.info("Generated new encryption keys for {} ", user.getUserName());
        });
    }

    public PasswordEncoder passwordEncoder() {
        return new UserPasswordEncoder();
    }

    @Transactional
    public void deleteUser(String userName) {
        userRepository.findByUserName(userName).ifPresent(userDTO -> {
            userConfigurationRepository.deleteByUser(userName);
            userRepository.delete(userDTO.getId());
            log.info("Deleted user {}", userDTO.getUserName());
        });
    }

    @Transactional
    public void newProtectionScheme(String userName, PasswordEncodingAlgorithm algorithm, CharSequence userPassword, Boolean removeOldSchemes) {
        Optional<UserDTO> userMaybe = userRepository.findByUserName(userName);
        userMaybe.ifPresent(userDTO -> {
            userConfigurationRepository.findByUser(userName).forEach(config -> invalidateOrRemove(removeOldSchemes, config));
            userConfigurationRepository.save(createUserConfigurationDTO(algorithm, userPassword, generateEncryptionKey(), userDTO));
            log.info("Updated default algorithm for {} to {}", userDTO.getUserName(), algorithm);
        });
    }

    public static String generateEncryptionKey() {
        return RandomStringUtils.randomAscii(32);
    }

    private void invalidateOrRemove(Boolean removeOldSchemes, UserConfigurationDTO config) {
        if (removeOldSchemes) {
            userConfigurationRepository.delete(config);
        } else {
            userConfigurationRepository.save(config.setActive(false));
        }
    }

    @Transactional
    public void save(UserDTO userDTO, UserConfigurationDTO... configurationDTO) {
        UserDTO savedUser = userRepository.save(userDTO);
        Stream.of(configurationDTO).forEach(userConfigurationRepository::save);
        log.info("Saved user: {}", savedUser.getUserName());
    }

    private UserConfigurationDTO createUserConfigurationDTO(PasswordEncodingAlgorithm algorithm, CharSequence password, String encryptionKey, UserDTO userDTO) {
        return new UserConfigurationDTO()
                .setActive(true).setAlgorithm(algorithm).setUser(userDTO.getUserName())
                .setDataProtectionKey(cryptoHelper.encryptData(password, encryptionKey));
    }

    private String generatePassword() {
        return RandomStringUtils.randomAscii(30).trim();
    }

    @RequiredArgsConstructor
    public enum ROLE {
        ADMIN(ADMIN_ROLE_VALUE), USER(USER_ROLE_VALUE);

        @Getter
        private final String value;

        public static ROLE fromDbValue(String value) {
            for (ROLE role : ROLE.values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
            return ROLE.valueOf(value);
        }
    }

}
