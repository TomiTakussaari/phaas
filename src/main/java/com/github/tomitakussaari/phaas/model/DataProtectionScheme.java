package com.github.tomitakussaari.phaas.model;

import lombok.*;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class DataProtectionScheme {
    private final int id;
    @NonNull
    private final PasswordEncodingAlgorithm algorithm;
    @NonNull
    private final String protectedEncryptionKey;

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
            return Encryptors.text(userPassword, salt()).decrypt(cryptPassword());
        }
        private String salt() {
            return scheme.protectedEncryptionKey.split(":::")[0];
        }

        private String cryptPassword() {
            return scheme.protectedEncryptionKey.split(":::")[1];
        }
    }

    @Data
    public static class PublicProtectionScheme {
        private final int id;
        @NonNull
        private final PasswordEncodingAlgorithm algorithm;
    }


}
