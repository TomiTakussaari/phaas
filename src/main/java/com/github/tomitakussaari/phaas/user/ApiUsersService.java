package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.dao.PhaasUserConfigurationRepository;
import com.github.tomitakussaari.phaas.user.dao.PhaasUserRepository;
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
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
public class ApiUsersService implements UserDetailsService {

    public static final String ADMIN_ROLE_VALUE = "ROLE_ADMIN";
    public static final String USER_ROLE_VALUE = "ROLE_USER";

    private final PhaasUserConfigurationRepository phaasUserConfigurationRepository;
    private final PhaasUserRepository phaasUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return phaasUserRepository.findByUserName(username)
                .map(userDTO -> new PhaasUserDetails(userDTO, phaasUserConfigurationRepository.findByUser(username)))
                .orElseThrow(() -> new UsernameNotFoundException("Not found: "+username));
    }

    public List<PhaasUserDetails> findAll() {
        List<PhaasUserDetails> details = new ArrayList<>();
        for(UserDTO user: phaasUserRepository.findAll()) {
            details.add(new PhaasUserDetails(user, phaasUserConfigurationRepository.findByUser(user.getUserName())));
        }
        return details;
    }

    boolean hasUsers() {
        return phaasUserRepository.count() > 0L;
    }

    public String createUser(String userName, PasswordEncodingAlgorithm algorithm, List<ROLE> roles) {
        return createUser(userName, algorithm, roles, generatePassword());
    }

    public String createUser(String userName, PasswordEncodingAlgorithm algorithm, List<ROLE> roles, String userPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserDTO userDTO = new UserDTO(null, userName, encoder.encode(userPassword), roles.stream().map(ROLE::getValue).collect(joining(",")));

        UserConfigurationDTO configurationDTO = getUserConfigurationDTO(algorithm, userPassword, userDTO);
        create(userDTO, configurationDTO);
        return userPassword;
    }

    @Transactional
    public void deleteUser(String userName) {
        phaasUserRepository.findByUserName(userName).ifPresent(userDTO -> {
            phaasUserConfigurationRepository.deleteByUser(userName);
            phaasUserRepository.delete(userDTO.getId());
            log.info("Deleted user {}", userDTO.getUserName());
        });
    }

    @Transactional
    public void newProtectionScheme(String userName, PasswordEncodingAlgorithm algorithm, CharSequence userPassword, Boolean removeOldSchemes) {
        Optional<UserDTO> userMaybe = phaasUserRepository.findByUserName(userName);
        userMaybe.ifPresent(userDTO -> {
            phaasUserConfigurationRepository.findByUser(userName).forEach(config -> invalidateOrRemove(removeOldSchemes, config));
            phaasUserConfigurationRepository.save(getUserConfigurationDTO(algorithm, userPassword, userDTO));
            log.info("Updated default algorithm for {} to {}", userDTO.getUserName(), algorithm);
        });
    }

    private void invalidateOrRemove(Boolean removeOldSchemes, UserConfigurationDTO config) {
        if(removeOldSchemes)  {
            phaasUserConfigurationRepository.delete(config);
        } else {
            phaasUserConfigurationRepository.save(config.setActive(false));
        }
    }

    @Transactional
    public void create(UserDTO userDTO, UserConfigurationDTO...configurationDTO) {
        UserDTO savedUser = phaasUserRepository.save(userDTO);
        Stream.of(configurationDTO).forEach(phaasUserConfigurationRepository::save);
        log.info("Created user: {}", savedUser.getUserName());
    }

    private String createEncryptionKey(CharSequence password) {
        String salt = KeyGenerators.string().generateKey();
        String encryptionKey = RandomStringUtils.random(30);
        TextEncryptor encryptor = Encryptors.text(password, salt);
        String encryptedKey = encryptor.encrypt(encryptionKey);
        return salt + ":::" + encryptedKey;
    }

    private UserConfigurationDTO getUserConfigurationDTO(PasswordEncodingAlgorithm algorithm, CharSequence password, UserDTO userDTO) {
        return new UserConfigurationDTO()
                .setActive(true).setAlgorithm(algorithm).setUser(userDTO.getUserName())
                .setDataProtectionKey(createEncryptionKey(password));
    }

    private String generatePassword() {
        return RandomStringUtils.randomAscii(30);
    }

    @RequiredArgsConstructor
    public enum ROLE {
        ADMIN(ADMIN_ROLE_VALUE), USER(USER_ROLE_VALUE);

        @Getter
        private final String value;

        public static ROLE fromDbValue(String value) {
            for(ROLE role: ROLE.values()) {
                if(role.value.equals(value)) {
                    return role;
                }
            }
            return ROLE.valueOf(value);
        }
    }

}
