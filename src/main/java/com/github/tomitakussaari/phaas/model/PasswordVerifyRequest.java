package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR;

@RequiredArgsConstructor
@Getter
public class PasswordVerifyRequest {
    @NonNull
    private final String passwordCandidate;
    @NonNull
    private final String hash;

    public int schemeId() {
        return Integer.valueOf(hash.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[0]);
    }

    public String encryptionSalt() {
        return hash.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
    }

    public String encryptedPasswordHash() {
        return getHash().split(ESCAPED_TOKEN_VALUE_SEPARATOR)[2];
    }
}
