package com.github.tomitakussaari.phaas.model;

import lombok.*;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class DataProtectionScheme {

    public static final String TOKEN_VALUE_SEPARATOR = ".";
    public static final String ESCAPED_TOKEN_VALUE_SEPARATOR = "\\"+TOKEN_VALUE_SEPARATOR;

    private final int id;
    @NonNull
    private final PasswordEncodingAlgorithm algorithm;
    @NonNull
    private final String encryptedKeyWithSalt;

    public PasswordEncoder passwordEncoder() {
        return algorithm.encoder();
    }

    public PublicProtectionScheme toPublicScheme() {
        return new PublicProtectionScheme(id, algorithm);
    }

    public CryptoData decryptedProtectionScheme(CharSequence userPassword) {
        return new CryptoData(this, userPassword);
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class CryptoData {
        private final DataProtectionScheme scheme;
        private final CharSequence userPassword;

        public DataProtectionScheme getScheme() {
            return scheme;
        }

        public String dataProtectionKey() {
            return encryptor(userPassword, saltPart()).decrypt(passwordPart());
        }
        private String saltPart() {
            return scheme.encryptedKeyWithSalt.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[0];
        }

        private String passwordPart() {
            return scheme.encryptedKeyWithSalt.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
        }

        public static TextEncryptor encryptor(CharSequence password, String salt) {
            return Encryptors.text(password, salt);
        }
    }

    @Data
    public static class PublicProtectionScheme {
        private final int id;
        @NonNull
        private final PasswordEncodingAlgorithm algorithm;
    }


}
