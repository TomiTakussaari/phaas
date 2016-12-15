package com.github.tomitakussaari.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class HashedPassword {
    private final String hash;

    public static HashedPassword from(Integer encryptionId, String encryptionSalt, String hashedPassword) {
        return new HashedPassword(encryptionId + ":::" + encryptionSalt + ":::" + hashedPassword);
    }
}
