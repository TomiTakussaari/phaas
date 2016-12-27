package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.TOKEN_VALUE_SEPARATOR;

@RequiredArgsConstructor
@Getter
public class HashedPassword {
    private final String hash;

    public static HashedPassword from(Integer encryptionId, String encryptionSalt, String hashedPassword) {
        return new HashedPassword(encryptionId + TOKEN_VALUE_SEPARATOR + encryptionSalt + TOKEN_VALUE_SEPARATOR + hashedPassword);
    }
}
