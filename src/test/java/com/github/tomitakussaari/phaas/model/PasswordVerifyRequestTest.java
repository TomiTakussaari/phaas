package com.github.tomitakussaari.phaas.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordVerifyRequestTest {

    @Test
    public void compatibleWithHashedPassword() {
        String hash = HashedPassword.from(12, "wannaBeSalt", "hashedPasswordWouldBeHere").getHash();
        assertThat(hash).isEqualTo("12.wannaBeSalt.hashedPasswordWouldBeHere");
        PasswordVerifyRequest request = new PasswordVerifyRequest("pw", hash);
        assertThat(request.getHash()).isEqualTo(hash);
        assertThat(request.encryptedPasswordHash()).isEqualTo("hashedPasswordWouldBeHere");
        assertThat(request.encryptionSalt()).isEqualTo("wannaBeSalt");
        assertThat(request.schemeId()).isEqualTo(12);
    }

    @Test(expected = NullPointerException.class)
    public void newInstanceCreationRequiresHash() {
        new PasswordVerifyRequest("password", null);
    }

    @Test(expected = NullPointerException.class)
    public void newInstanceCreationRequiresPassword() {
        new PasswordVerifyRequest(null, "hash");
    }
}
