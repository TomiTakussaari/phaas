package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationRepository;
import com.github.tomitakussaari.phaas.user.dao.UserRepository;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.TOKEN_VALUE_SEPARATOR;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
public class UsersService implements UserDetailsService {

    public static final String ADMIN_ROLE_VALUE = "ROLE_ADMIN";
    public static final String USER_ROLE_VALUE = "ROLE_USER";

    private final UserConfigurationRepository userConfigurationRepository;
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .map(userDTO -> new PhaasUserDetails(userDTO, userConfigurationRepository.findByUser(username)))
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));
    }

    public List<PhaasUserDetails> findAll() {
        List<PhaasUserDetails> details = new ArrayList<>();
        for (UserDTO user : userRepository.findAll()) {
            details.add(new PhaasUserDetails(user, userConfigurationRepository.findByUser(user.getUserName())));
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

        String encryptionKey = RandomStringUtils.randomAlphanumeric(30);
        UserConfigurationDTO configurationDTO = getUserConfigurationDTO(algorithm, userPassword, encryptionKey, userDTO);
        save(userDTO, configurationDTO);
        return userPassword;
    }

    @Transactional
    public void changePasswordAndSecret(String userName, CharSequence newPassword, CharSequence oldPassword, Optional<String> sharedSecretForSigningCommunication) {
        Optional<UserDTO> updatedUserMaybe = userRepository.findByUserName(userName)
                .map(user -> user.setPasswordHash(passwordEncoder().encode(newPassword)))
                .map(user -> sharedSecretForSigningCommunication.map(user::setSharedSecretForSigningCommunication).orElse(user));

        updatedUserMaybe.ifPresent(user -> {
            List<UserConfigurationDTO> configs = userConfigurationRepository.findByUser(user.getUserName());
            List<UserConfigurationDTO> newConfigs = configs.stream().map(config -> {
                String protectionKey = config.toProtectionScheme().decryptedProtectionScheme(oldPassword).dataProtectionKey();
                UserConfigurationDTO newConfig = getUserConfigurationDTO(config.getAlgorithm(), newPassword, protectionKey, user);
                newConfig.setId(config.getId());
                return newConfig;
            }).collect(Collectors.toList());

            save(user, newConfigs.toArray(new UserConfigurationDTO[]{}));
            log.info("Updated passwords for {} ", user.getUserName());
        });
    }

    private BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
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
            String encryptionKey = RandomStringUtils.randomAlphanumeric(30);
            userConfigurationRepository.findByUser(userName).forEach(config -> invalidateOrRemove(removeOldSchemes, config));
            userConfigurationRepository.save(getUserConfigurationDTO(algorithm, userPassword, encryptionKey, userDTO));
            log.info("Updated default algorithm for {} to {}", userDTO.getUserName(), algorithm);
        });
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

    private String createEncryptionKey(CharSequence password, String encryptionKey) {
        String salt = KeyGenerators.string().generateKey();

        TextEncryptor encryptor = DataProtectionScheme.CryptoData.encryptor(password, salt);
        return salt + TOKEN_VALUE_SEPARATOR + encryptor.encrypt(encryptionKey);
    }

    private UserConfigurationDTO getUserConfigurationDTO(PasswordEncodingAlgorithm algorithm, CharSequence password, String encryptionKey, UserDTO userDTO) {
        return new UserConfigurationDTO()
                .setActive(true).setAlgorithm(algorithm).setUser(userDTO.getUserName())
                .setDataProtectionKey(createEncryptionKey(password, encryptionKey));
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