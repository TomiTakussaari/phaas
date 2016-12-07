package com.github.tomitakussaari.util;

import com.github.tomitakussaari.model.HashedPassword;
import com.github.tomitakussaari.model.PasswordHashRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

    public HashedPassword hash(PasswordHashRequest request, String pepper) {
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.text(pepper, salt);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);

        String hashedPassword = encoder.encode(DigestUtils.sha256Hex(request.getRawPassword()));
        return new HashedPassword(encryptor.encrypt(hashedPassword));
    }
}
