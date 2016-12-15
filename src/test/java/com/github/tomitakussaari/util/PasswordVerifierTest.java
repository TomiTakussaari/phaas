package com.github.tomitakussaari.util;

import com.github.tomitakussaari.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordVerifierTest {

    private final PasswordHasher hasher = new PasswordHasher();
    private final PasswordVerifier verifier = new PasswordVerifier();
    private final ProtectionScheme protectionScheme = new ProtectionScheme(1, "DEFAULT_SHA256ANDBCRYPT", "encryption_key");

    @Test
    public void verifiesHashCreatedByPasswordHasher() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), protectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), protectionScheme);
        assertTrue(result.isValid());
    }

    @Test
    public void wrongPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), protectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("wrong-password", hash.getHash()), protectionScheme);
        assertFalse(result.isValid());
    }

    @Test
    public void emptyPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), protectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("", hash.getHash()), protectionScheme);
        assertFalse(result.isValid());
    }

    @Test
    public void noticesDifferencesWithVeryLongPasswords() {
        String longPassword = RandomStringUtils.randomAlphanumeric(200);
        HashedPassword hash = hasher.hash(new PasswordHashRequest(longPassword + "-test"), protectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest(longPassword, hash.getHash()), protectionScheme);
        assertFalse(result.isValid());
    }

    @Test
    public void verifiesExistingHash() {
        String hash = "1:::64e68551653223cb:::8bd0e4de5c6b0ab83d5f01b7a2b79ea59f41e24cdcb39e65de6106249190e061159cb4d19b03bd40582b4cc530e0a437a0630eb97ae0a2f89987214dc43a8fec32c04a7a565e328422ef4786ff64423c";
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash), protectionScheme);
        assertTrue(result.isValid());
    }
}