package com.github.tomitakussaari.phaas.model;

import com.google.common.hash.Hashing;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Constants;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

public enum PasswordEncodingAlgorithm {
    ARGON2("$argon2i$", Argon2PasswordEncoder::new),
    SHA256_BCRYPT("$2a$", SHA256AndBCryptPasswordEncoder::new);

    private final String hashPrefix;
    private final Supplier<PasswordEncoder> encoderSupplier;

    PasswordEncodingAlgorithm(String hashPrefix, Supplier<PasswordEncoder> encoderSupplier) {
        this.hashPrefix = hashPrefix;
        this.encoderSupplier = encoderSupplier;
    }

    public PasswordEncoder encoder() {
        return encoderSupplier.get();
    }

    public static Optional<PasswordEncodingAlgorithm> findForHash(String hash) {
        for (PasswordEncodingAlgorithm algorithm : values()) {
            if (hash.startsWith(algorithm.hashPrefix)) {
                return Optional.of(algorithm);
            }
        }
        return Optional.empty();
    }
}

class Argon2PasswordEncoder implements PasswordEncoder {

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2i, Argon2Constants.DEFAULT_SALT_LENGTH, Argon2Constants.DEFAULT_HASH_LENGTH);

    @Override
    public String encode(CharSequence rawPassword) {
        return argon2.hash(2, 65536, 2, rawPassword.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return argon2.verify(encodedPassword, rawPassword.toString(), StandardCharsets.UTF_8);
    }
}

class SHA256AndBCryptPasswordEncoder extends BCryptPasswordEncoder {

    SHA256AndBCryptPasswordEncoder() {
        super(11);
    }

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