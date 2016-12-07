package com.github.tomitakussaari.model;

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

    public int encryptionKeyId() {
        return Integer.valueOf(hash.substring(0, 1));
    }
}
