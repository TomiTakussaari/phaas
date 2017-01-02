package com.github.tomitakussaari.phaas.model;

import com.github.tomitakussaari.phaas.util.CryptoHelper;
import lombok.*;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Getter
public class DataProtectionScheme {

    public static final String TOKEN_VALUE_SEPARATOR = ".";
    public static final String ESCAPED_TOKEN_VALUE_SEPARATOR = "\\" + TOKEN_VALUE_SEPARATOR;

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

    public CryptoData cryptoData(CharSequence userPassword) {
        return new CryptoData(this, userPassword);
    }

    @RequiredArgsConstructor
    public static class CryptoData {
        private final DataProtectionScheme scheme;
        private final CharSequence userPassword;
        private final CryptoHelper cryptoHelper = new CryptoHelper();

        public DataProtectionScheme getScheme() {
            return scheme;
        }

        public String dataProtectionKey() {
            return cryptoHelper.decryptData(userPassword, scheme.getEncryptedKeyWithSalt());
        }
    }

    @Data
    public static class PublicProtectionScheme {
        private final int id;
        @NonNull
        private final PasswordEncodingAlgorithm algorithm;
    }


}
