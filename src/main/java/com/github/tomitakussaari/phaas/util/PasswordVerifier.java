package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.PasswordHashRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyResult;
import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.Optional;

public class PasswordVerifier {

    private final PasswordHasher passwordReHasher = new PasswordHasher();

    public PasswordVerifyResult verify(PasswordVerifyRequest request, ProtectionScheme schemeForRequest, ProtectionScheme activeSchemeForUser, String encryptionKey) {
        TextEncryptor decryptor = Encryptors.text(encryptionKey, request.encryptionSalt());
        String hashedPassword = decryptor.decrypt(request.encryptedPasswordHash());
        boolean passwordValid = schemeForRequest.passwordEncoder().matches(request.getPasswordCandidate(), hashedPassword);
        Optional<String> upgradedHash = getUpgradedHash(request, schemeForRequest, activeSchemeForUser, passwordValid, encryptionKey);
        return new PasswordVerifyResult(upgradedHash, passwordValid);
    }

    private Optional<String> getUpgradedHash(PasswordVerifyRequest request, ProtectionScheme currentScheme, ProtectionScheme newScheme, boolean passwordValid, String encryptionKey) {
        if(!currentScheme.equals(newScheme) && passwordValid) {
            return Optional.of(passwordReHasher.hash(new PasswordHashRequest(request.getPasswordCandidate()), newScheme, encryptionKey).getHash());
        }
        return Optional.empty();
    }
}
