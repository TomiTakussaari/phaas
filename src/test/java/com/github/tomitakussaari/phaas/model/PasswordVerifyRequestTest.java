package com.github.tomitakussaari.phaas.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordVerifyRequestTest {

    @Test
    public void compatibleWithHashedPassword() {
        String hash = HashedPassword.from(12, "wannaBeSalt", "hashedPasswordWouldBeHere").getHash();
        assertEquals("12:::wannaBeSalt:::hashedPasswordWouldBeHere", hash);
        PasswordVerifyRequest request = new PasswordVerifyRequest("pw", hash);
        assertEquals(hash, request.getHash());
        assertEquals("hashedPasswordWouldBeHere", request.encryptedPasswordHash());
        assertEquals("wannaBeSalt", request.encryptionSalt());
        assertEquals(12, request.schemeId());
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