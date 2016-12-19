package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class PasswordVerifyResult {
    private final Optional<String> upgradedHash;
    private final boolean valid;
}
