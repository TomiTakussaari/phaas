package com.github.tomitakussaari.util;

import com.github.tomitakussaari.model.PasswordHashRequest;
import com.github.tomitakussaari.model.PasswordVerifyRequest;
import com.github.tomitakussaari.model.PasswordVerifyResult;
import com.github.tomitakussaari.model.ProtectionScheme;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.Optional;

public class PasswordVerifier {

    private final PasswordHasher passwordReHasher = new PasswordHasher();

    public PasswordVerifyResult verify(PasswordVerifyRequest request, ProtectionScheme schemeForRequest, ProtectionScheme activeSchemeForUser) {
        TextEncryptor decryptor = Encryptors.text(schemeForRequest.getEncryptionKey(), request.encryptionSalt());
        String hashedPassword = decryptor.decrypt(request.encryptedPasswordHash());
        boolean passwordValid = schemeForRequest.passwordEncoder().matches(request.getPasswordCandidate(), hashedPassword);
        Optional<String> upgradedHash = getUpgradedHash(request, schemeForRequest, activeSchemeForUser, passwordValid);
        return new PasswordVerifyResult(upgradedHash, passwordValid);
    }

    private Optional<String> getUpgradedHash(PasswordVerifyRequest request, ProtectionScheme schemeForRequest, ProtectionScheme activeSchemeForUser, boolean passwordValid) {
        return passwordValid ? rehashPasswordIfNeeded(request.getPasswordCandidate(), schemeForRequest, activeSchemeForUser) : Optional.empty();
    }

    private Optional<String> rehashPasswordIfNeeded(String password, ProtectionScheme currentScheme, ProtectionScheme newScheme) {
        if(!currentScheme.equals(newScheme)) {
            return Optional.of(passwordReHasher.hash(new PasswordHashRequest(password), newScheme).getHash());
        }
        return Optional.empty();
    }
}
