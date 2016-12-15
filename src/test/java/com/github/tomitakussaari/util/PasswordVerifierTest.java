package com.github.tomitakussaari.util;

import com.github.tomitakussaari.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordVerifierTest {

    private final PasswordHasher hasher = new PasswordHasher();
    private final PasswordVerifier verifier = new PasswordVerifier();
    private final ProtectionScheme currentProtectionScheme = new ProtectionScheme(1, "DEFAULT_SHA256ANDBCRYPT", "encryption_key");
    private final ProtectionScheme newProtectionScheme = new ProtectionScheme(1, "DEFAULT_SHA256ANDBCRYPT", "encryption_key2");

    @Test
    public void doesNotReturnRehashedPasswordWhenActiveSchemeIsSameAsCurrent() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, currentProtectionScheme);
        assertTrue(result.isValid());
        assertFalse(result.getUpgradedHash().isPresent());
    }

    @Test
    public void returnsRehashedPasswordWhenActiveSchemeIsNotSameAsCurrent() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, newProtectionScheme);
        assertTrue(result.isValid());
        assertTrue(result.getUpgradedHash().isPresent());
    }

    @Test
    public void doesNotReturnRehashedPasswordWhenPasswordIsNotCorrect() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password2", hash.getHash()), currentProtectionScheme, newProtectionScheme);
        assertFalse(result.isValid());
        assertFalse(result.getUpgradedHash().isPresent());
    }

    @Test
    public void rehashedPasswordHashIsValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        String rehashedPassword = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, newProtectionScheme).getUpgradedHash().get();
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", rehashedPassword), newProtectionScheme, newProtectionScheme);
        assertTrue(result.isValid());
        assertFalse(result.getUpgradedHash().isPresent());
    }

    @Test
    public void verifiesHashCreatedByPasswordHasher() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, currentProtectionScheme);
        assertTrue(result.isValid());
    }

    @Test
    public void wrongPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("wrong-password", hash.getHash()), currentProtectionScheme, currentProtectionScheme);
        assertFalse(result.isValid());
    }

    @Test
    public void emptyPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("", hash.getHash()), currentProtectionScheme, currentProtectionScheme);
        assertFalse(result.isValid());
    }

    @Test
    public void noticesDifferencesWithVeryLongPasswords() {
        String longPassword = RandomStringUtils.randomAlphanumeric(200);
        HashedPassword hash = hasher.hash(new PasswordHashRequest(longPassword + "-test"), currentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest(longPassword, hash.getHash()), currentProtectionScheme, currentProtectionScheme);
        assertFalse(result.isValid());
    }

    @Test
    public void verifiesExistingHash() {
        String hash = "1:::64e68551653223cb:::8bd0e4de5c6b0ab83d5f01b7a2b79ea59f41e24cdcb39e65de6106249190e061159cb4d19b03bd40582b4cc530e0a437a0630eb97ae0a2f89987214dc43a8fec32c04a7a565e328422ef4786ff64423c";
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash), currentProtectionScheme, currentProtectionScheme);
        assertTrue(result.isValid());
    }
}