package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordVerifierTest {

    private final PasswordHasher hasher = new PasswordHasher();
    private final PasswordVerifier verifier = new PasswordVerifier();
    private final String password = "password";
    private final String encryptionKey = "encryption_key";
    private final String encryptionKeyTwo = "encryption_key2";
    private static final CryptoHelper cryptoHelper = new CryptoHelper(new PepperSource(""));
    private final DataProtectionScheme currentDataProtectionScheme = new DataProtectionScheme(1, PasswordEncodingAlgorithm.SHA256_BCRYPT, cryptoHelper.encryptData(password, encryptionKey), cryptoHelper);
    private final DataProtectionScheme newDataProtectionScheme = new DataProtectionScheme(2, PasswordEncodingAlgorithm.SHA256_BCRYPT, cryptoHelper.encryptData(password, encryptionKeyTwo), cryptoHelper);
    private final DataProtectionScheme.CryptoData decryptedCurrentProtectionScheme = currentDataProtectionScheme.cryptoData(password);
    private final DataProtectionScheme.CryptoData decryptedNewProtectionScheme = newDataProtectionScheme.cryptoData(password);

    @Test
    public void doesNotReturnRehashedPasswordWhenActiveSchemeIsSameAsCurrent() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest(password), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), decryptedCurrentProtectionScheme, decryptedCurrentProtectionScheme);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getUpgradedHash().isPresent()).isFalse();
    }

    @Test
    public void returnsRehashedPasswordWhenActiveSchemeIsNotSameAsCurrent() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), decryptedCurrentProtectionScheme, decryptedNewProtectionScheme);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getUpgradedHash().isPresent()).isTrue();
    }

    @Test
    public void doesNotReturnRehashedPasswordWhenPasswordIsNotCorrect() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password2", hash.getHash()), decryptedCurrentProtectionScheme, decryptedNewProtectionScheme);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getUpgradedHash().isPresent()).isFalse();
    }

    @Test
    public void rehashedPasswordHashIsValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), decryptedCurrentProtectionScheme);
        String rehashedPassword = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), decryptedCurrentProtectionScheme, decryptedNewProtectionScheme).getUpgradedHash().get();
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", rehashedPassword), decryptedNewProtectionScheme, decryptedNewProtectionScheme);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getUpgradedHash().isPresent()).isFalse();
    }

    @Test
    public void verifiesHashCreatedByPasswordHasher() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash.getHash()), decryptedCurrentProtectionScheme, decryptedNewProtectionScheme);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void wrongPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("wrong-password", hash.getHash()), decryptedCurrentProtectionScheme, decryptedCurrentProtectionScheme);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void emptyPasswordIsNotValid() {
        HashedPassword hash = hasher.hash(new PasswordHashRequest("password"), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("", hash.getHash()), decryptedCurrentProtectionScheme, decryptedCurrentProtectionScheme);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void noticesDifferencesWithVeryLongPasswords() {
        String longPassword = RandomStringUtils.randomAlphanumeric(200);
        HashedPassword hash = hasher.hash(new PasswordHashRequest(longPassword + "-test"), decryptedCurrentProtectionScheme);
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest(longPassword, hash.getHash()), decryptedCurrentProtectionScheme, decryptedCurrentProtectionScheme);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void verifiesExistingHash() {
        String hash = "1.64e68551653223cb.8bd0e4de5c6b0ab83d5f01b7a2b79ea59f41e24cdcb39e65de6106249190e061159cb4d19b03bd40582b4cc530e0a437a0630eb97ae0a2f89987214dc43a8fec32c04a7a565e328422ef4786ff64423c";
        PasswordVerifyResult result = verifier.verify(new PasswordVerifyRequest("password", hash), decryptedCurrentProtectionScheme, decryptedCurrentProtectionScheme);
        assertThat(result.isValid()).isTrue();
    }
}
