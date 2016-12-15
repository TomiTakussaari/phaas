package com.github.tomitakussaari.util;

import com.github.tomitakussaari.model.PasswordVerifyRequest;
import com.github.tomitakussaari.model.PasswordVerifyResult;
import com.github.tomitakussaari.model.ProtectionScheme;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.Optional;

public class PasswordVerifier {

    public PasswordVerifyResult verify(PasswordVerifyRequest request, ProtectionScheme scheme) {
        TextEncryptor decryptor = Encryptors.text(scheme.getEncryptionKey(), request.encryptionSalt());
        String hashedPassword = decryptor.decrypt(request.encryptedPasswordHash());
        boolean passwordValid = scheme.passwordEncoder().matches(request.getPasswordCandidate(), hashedPassword);
        return new PasswordVerifyResult(Optional.empty(), passwordValid);
    }
}
