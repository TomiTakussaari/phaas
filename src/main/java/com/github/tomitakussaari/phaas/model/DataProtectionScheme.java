package com.github.tomitakussaari.phaas.model;

import com.github.tomitakussaari.phaas.util.CryptoHelper;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
    @NonNull
    private final CryptoHelper cryptoHelper;

    public PasswordEncoder passwordEncoder() {
        return getAlgorithm().encoder();
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
        @Getter(lazy = true)
        private final String dataProtectionKey  = dataProtectionKey();

        public DataProtectionScheme getScheme() {
            return scheme;
        }

        private String dataProtectionKey() {
            return scheme.getCryptoHelper().decryptData(userPassword, scheme.getEncryptedKeyWithSalt());
        }
    }

    @Data
    public static class PublicProtectionScheme {
        private final int id;
        @NonNull
        private final PasswordEncodingAlgorithm algorithm;
    }


}
