package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.model.HashedPassword;
import com.github.tomitakussaari.phaas.model.PasswordHashRequest;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

public class PasswordHasher {

    public HashedPassword hash(PasswordHashRequest request, CryptoData protectionScheme) {
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.text(protectionScheme.getDataProtectionKey(), salt);
        String hashedPassword = protectionScheme.getScheme().passwordEncoder().encode(request.getRawPassword());
        return HashedPassword.from(protectionScheme.getScheme().getId(), salt, encryptor.encrypt(hashedPassword));
    }
}
