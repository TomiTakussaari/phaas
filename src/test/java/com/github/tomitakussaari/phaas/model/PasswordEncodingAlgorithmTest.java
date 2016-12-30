package com.github.tomitakussaari.phaas.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordEncodingAlgorithmTest {

    @Test
    public void findForHashReturnsEmptyForUnknown() {
        assertFalse(PasswordEncodingAlgorithm.findForHash("foobar").isPresent());
    }

    @Test
    public void canHashAndVerifyWithAllAlgorithms() {
        for(PasswordEncodingAlgorithm algorithm : PasswordEncodingAlgorithm.values()) {
            PasswordEncoder encoder = algorithm.encoder();
            String password = "password";
            String hash = encoder.encode(password);
            assertTrue(algorithm+ " did not verify password", encoder.matches(password, hash));
        }
    }

    @Test
    public void findsCorrectEncoderForHash() {
        for(PasswordEncodingAlgorithm algorithm : PasswordEncodingAlgorithm.values()) {
            String password = "password";
            String hash = algorithm.encoder().encode(password);
            PasswordEncoder decoder = PasswordEncodingAlgorithm.findForHash(hash).get().encoder();
            assertTrue(algorithm+ " did not verify password", decoder.matches(password, hash));
        }
    }


    @Test
    public void allAlgorithmsSupportVeryLongPasswords() {
        for(PasswordEncodingAlgorithm algorithm : PasswordEncodingAlgorithm.values()) {
            PasswordEncoder encoder = algorithm.encoder();
            String password = RandomStringUtils.randomAlphabetic(120);
            String hash = encoder.encode(password);
            assertTrue(algorithm+ " did not verify password", encoder.matches(password, hash));
            assertFalse(algorithm+ " did not notice difference with very long password", encoder.matches(password+"1", hash));
        }
    }

}