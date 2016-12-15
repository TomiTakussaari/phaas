package com.github.tomitakussaari.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordVerifyRequestTest {

    @Test
    public void compatibleWithHashedPassword() {
        String hash = HashedPassword.from(12, "wannaBeSalt", "hashedPasswordWouldBeHere").getHash();
        assertEquals("12:::wannaBeSalt:::hashedPasswordWouldBeHere", hash);
        PasswordVerifyRequest request = new PasswordVerifyRequest("pw", hash);
        assertEquals("hashedPasswordWouldBeHere", request.encryptedPasswordHash());
        assertEquals("wannaBeSalt", request.encryptionSalt());
        assertEquals(12, request.schemeId());
    }
}