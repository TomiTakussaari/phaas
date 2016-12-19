package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordVerifierTest {

    private final PasswordHasher hasher = new PasswordHasher();
    private final PasswordVerifier verifier = new PasswordVerifier();
    private final String salt = KeyGenerators.string().generateKey();
    private final String password = "password";
    private final ProtectionScheme currentProtectionScheme = new ProtectionScheme(1, ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT, salt+":::"+Encryptors.text(password, salt).encrypt("encryption_key"));
    private final ProtectionScheme newProtectionScheme = new ProtectionScheme(1, ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT, salt+":::"+Encryptors.text(password, salt).encrypt("encryption_key2"));


    @Test
    public void doesNotReturnRehashedPasswordWhenActiveSchemeIsSameAsCurrent() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest(password), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, currentProtectionScheme, password);
        assertTrue(result.isValid());
        assertFalse(result.getUpgradedHash().isPresent());
    }

    @Test
    public void returnsRehashedPasswordWhenActiveSchemeIsNotSameAsCurrent() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, newProtectionScheme, password);
        assertTrue(result.isValid());
        assertTrue(result.getUpgradedHash().isPresent());
    }

    @Test
    public void doesNotReturnRehashedPasswordWhenPasswordIsNotCorrect() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password2", hash.getHash()), currentProtectionScheme, newProtectionScheme, password);
        assertFalse(result.isValid());
        assertFalse(result.getUpgradedHash().isPresent());
    }

    @Test
    public void rehashedPasswordHashIsValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme, password);
        String rehashedPassword = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, newProtectionScheme, password).getUpgradedHash().get();
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", rehashedPassword), newProtectionScheme, newProtectionScheme, password);
        assertTrue(result.isValid());
        assertFalse(result.getUpgradedHash().isPresent());
    }

    @Test
    public void verifiesHashCreatedByPasswordHasher() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), currentProtectionScheme, currentProtectionScheme, password);
        assertTrue(result.isValid());
    }

    @Test
    public void wrongPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("wrong-password", hash.getHash()), currentProtectionScheme, currentProtectionScheme, password);
        assertFalse(result.isValid());
    }

    @Test
    public void emptyPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("", hash.getHash()), currentProtectionScheme, currentProtectionScheme, password);
        assertFalse(result.isValid());
    }

    @Test
    public void noticesDifferencesWithVeryLongPasswords() {
        String longPassword = RandomStringUtils.randomAlphanumeric(200);
        HashedPassword hash = hasher.hash(new PasswordHashRequest(longPassword + "-test"), currentProtectionScheme, password);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest(longPassword, hash.getHash()), currentProtectionScheme, currentProtectionScheme, password);
        assertFalse(result.isValid());
    }

    @Test
    public void verifiesExistingHash() {
        String hash = "1:::64e68551653223cb:::8bd0e4de5c6b0ab83d5f01b7a2b79ea59f41e24cdcb39e65de6106249190e061159cb4d19b03bd40582b4cc530e0a437a0630eb97ae0a2f89987214dc43a8fec32c04a7a565e328422ef4786ff64423c";
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash), currentProtectionScheme, currentProtectionScheme, password);
        assertTrue(result.isValid());
    }
}