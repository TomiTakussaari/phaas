package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.model.PasswordHashRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyResult;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.Optional;

public class PasswordVerifier {

    private final PasswordHasher passwordReHasher = new PasswordHasher();

    public PasswordVerifyResult verify(PasswordVerifyRequest request, CryptoData schemeForRequest, CryptoData activeSchemeForUser) {
        TextEncryptor decryptor = Encryptors.text(schemeForRequest.dataProtectionKey(), request.encryptionSalt());
        String hashedPassword = decryptor.decrypt(request.encryptedPasswordHash());
        boolean passwordValid = schemeForRequest.getScheme().passwordEncoder().matches(request.getPasswordCandidate(), hashedPassword);
        Optional<String> upgradedHash = getUpgradedHash(request, schemeForRequest, activeSchemeForUser, passwordValid);
        return new PasswordVerifyResult(upgradedHash, passwordValid);
    }

    private Optional<String> getUpgradedHash(PasswordVerifyRequest request, CryptoData currentScheme, CryptoData newScheme, boolean passwordValid) {
        if (currentScheme.getScheme().getId() != newScheme.getScheme().getId() && passwordValid) {
            return Optional.of(passwordReHasher.hash(new PasswordHashRequest(request.getPasswordCandidate()), newScheme).getHash());
        }
        return Optional.empty();
    }
}
