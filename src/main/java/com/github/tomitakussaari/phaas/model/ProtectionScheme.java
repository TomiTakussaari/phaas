package com.github.tomitakussaari.phaas.model;

import com.google.common.hash.Hashing;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class ProtectionScheme {
    private final int id;
    @NonNull
    private final PasswordEncodingAlgorithm algorithm;
    @NonNull
    private final String encryptionKey;

    public PasswordEncoder passwordEncoder() {
        return algorithm.encoder();
    }

    public PublicProtectionScheme toPublicScheme() {
        return new PublicProtectionScheme(id, algorithm);
    }

    @Data
    public static class PublicProtectionScheme {
        private final int id;
        @NonNull
        private final ProtectionScheme.PasswordEncodingAlgorithm algorithm;
    }

    public enum PasswordEncodingAlgorithm {
        DEFAULT_SHA256ANDBCRYPT(SHA256AndBCryptPasswordEncoder::new);

        private final Supplier<PasswordEncoder> supplier;

        PasswordEncodingAlgorithm(Supplier<PasswordEncoder> supplier) {
            this.supplier = supplier;
        }

        public PasswordEncoder encoder() {
            return supplier.get();
        }
    }

    static class SHA256AndBCryptPasswordEncoder extends BCryptPasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return super.encode(sha256hex(rawPassword));
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return super.matches(sha256hex(rawPassword), encodedPassword);
        }

        static String sha256hex(CharSequence toHash) {
            return Hashing.sha256().hashString(toHash, StandardCharsets.UTF_8).toString();
        }
    }
}
