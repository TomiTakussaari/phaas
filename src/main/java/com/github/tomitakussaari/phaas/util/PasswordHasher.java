package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.HashedPassword;
import com.github.tomitakussaari.phaas.model.PasswordHashRequest;
import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

public class PasswordHasher {

    public HashedPassword hash(PasswordHashRequest request, ProtectionScheme protectionScheme, String userPassword) {
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.text(protectionScheme.decryptDataProtectionKey(userPassword), salt);
        String hashedPassword = protectionScheme.passwordEncoder().encode(request.getRawPassword());
        return HashedPassword.from(protectionScheme.getId(), salt, encryptor.encrypt(hashedPassword));
    }
}
