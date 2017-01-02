package com.github.tomitakussaari.phaas.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordEncodingAlgorithmTest {

    @Test
    public void findForHashReturnsEmptyForUnknown() {
        assertThat(PasswordEncodingAlgorithm.findForHash("foobar").isPresent()).isFalse();
    }

    @Test
    public void canHashAndVerifyWithAllAlgorithms() {
        for (PasswordEncodingAlgorithm algorithm : PasswordEncodingAlgorithm.values()) {
            PasswordEncoder encoder = algorithm.encoder();
            String password = "password";
            String hash = encoder.encode(password);
            assertThat(encoder.matches(password, hash)).isTrue();
        }
    }

    @Test
    public void findsCorrectEncoderForHash() {
        for (PasswordEncodingAlgorithm algorithm : PasswordEncodingAlgorithm.values()) {
            String password = "password";
            String hash = algorithm.encoder().encode(password);
            PasswordEncoder decoder = PasswordEncodingAlgorithm.findForHash(hash).get().encoder();
            assertThat(decoder.matches(password, hash)).isTrue();
        }
    }


    @Test
    public void allAlgorithmsSupportVeryLongPasswords() {
        for (PasswordEncodingAlgorithm algorithm : PasswordEncodingAlgorithm.values()) {
            PasswordEncoder encoder = algorithm.encoder();
            String password = RandomStringUtils.randomAlphabetic(120);
            String hash = encoder.encode(password);
            assertThat(encoder.matches(password, hash)).isTrue();
            assertThat(encoder.matches(password + "1", hash)).isFalse();
        }
    }

}
