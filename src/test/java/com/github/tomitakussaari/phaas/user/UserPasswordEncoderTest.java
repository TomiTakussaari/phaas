package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserPasswordEncoderTest {

    private UserPasswordEncoder encoder = new UserPasswordEncoder();
    private String password = "password";
    @Test
    public void hashesAndVerifiesPassword() {
        String hash = encoder.encode(password);
        assertTrue(encoder.matches(password, hash));
    }

    @Test
    public void hashesNewPasswordsWithArgon2() {
        String hash = encoder.encode(password);
        System.out.println(hash);
        assertEquals(PasswordEncodingAlgorithm.ARGON2, PasswordEncodingAlgorithm.findForHash(hash).get());
    }

    @Test
    public void acceptsBCryptProtectedPassword() {
        String hash = PasswordEncodingAlgorithm.SHA256_BCRYPT.encoder().encode(password);
        assertTrue(encoder.matches(password, hash));
    }

}