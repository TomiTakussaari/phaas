package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PasswordVerifyRequest {
    @NonNull
    private final String passwordCandidate;
    @NonNull
    private final String hash;

    public int schemeId() {
        return Integer.valueOf(hash.split(":::")[0]);
    }

    public String encryptionSalt() {
        return hash.split(":::")[1];
    }

    public String encryptedPasswordHash() {
        return hash.split(":::")[2];
    }
}
