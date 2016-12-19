package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import com.github.tomitakussaari.phaas.user.PhaasUserConfigurationRepository.UserConfigurationDTO;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
public class ApiUsersService implements UserDetailsService {

    public static final String ADMIN_ROLE_VALUE = "ROLE_ADMIN";
    public static final String USER_ROLE_VALUE = "ROLE_USER";
    private final PhaasUserConfigurationRepository phaasUserConfigurationRepository;
    private final PhaasUserRepository phaasUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PhaasUserRepository.UserDTO userDTO = phaasUserRepository.findByUserName(username);
        if (userDTO == null) {
            throw new UsernameNotFoundException("not found: " + username);
        }
        return new PhaasUserDetails(userDTO, phaasUserConfigurationRepository.findByUserName(username));
    }

    @Transactional
    public void createUser(String userName, ProtectionScheme.PasswordEncodingAlgorithm algorithm, List<ROLE> roles, String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        PhaasUserRepository.UserDTO userDTO = new PhaasUserRepository.UserDTO(userName, encoder.encode(password), roles.stream().map(ROLE::getValue).collect(Collectors.joining(",")));

        UserConfigurationDTO configurationDTO = UserConfigurationDTO.builder()
                .active(true).algorithm(algorithm).userName(userName)
                .dataProtectionKey(createEncryptionKey(password))
                .build();
        create(userDTO, configurationDTO);
    }

    void create(PhaasUserRepository.UserDTO userDTO, UserConfigurationDTO...configurationDTO) {
        PhaasUserRepository.UserDTO savedUser = phaasUserRepository.save(userDTO);
        Stream.of(configurationDTO).forEach(phaasUserConfigurationRepository::save);
        log.info("Created user: {}", savedUser.getUserName());
    }

    private String createEncryptionKey(String password) {
        String salt = KeyGenerators.string().generateKey();
        String encryptionKey = RandomStringUtils.random(20);
        TextEncryptor encryptor = Encryptors.text(password, salt);
        String encryptedKey = encryptor.encrypt(encryptionKey);
        return salt + ":::" + encryptedKey;
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
