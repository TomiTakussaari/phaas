package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPasswordEncoderTest {

    private UserPasswordEncoder encoder = new UserPasswordEncoder();
    private String password = "password";

    @Test
    public void hashesAndVerifiesPassword() {
        String hash = encoder.encode(password);
        assertThat(encoder.matches(password, hash)).isTrue();
    }

    @Test
    public void hashesNewPasswordsWithArgon2() {
        String hash = encoder.encode(password);
        System.out.println(hash);
        assertThat(PasswordEncodingAlgorithm.findForHash(hash).get()).isEqualTo(PasswordEncodingAlgorithm.ARGON2);
    }

    @Test
    public void acceptsBCryptProtectedPassword() {
        String hash = PasswordEncodingAlgorithm.SHA256_BCRYPT.encoder().encode(password);
        assertThat(encoder.matches(password, hash)).isTrue();
    }

}
