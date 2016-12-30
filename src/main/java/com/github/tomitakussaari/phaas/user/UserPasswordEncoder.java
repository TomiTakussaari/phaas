package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return PasswordEncodingAlgorithm.ARGON2.encoder().encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return PasswordEncodingAlgorithm.findForHash(encodedPassword)
                .map(PasswordEncodingAlgorithm::encoder)
                .map(passwordEncoder -> passwordEncoder.matches(rawPassword, encodedPassword))
                .orElse(false);
    }
}
