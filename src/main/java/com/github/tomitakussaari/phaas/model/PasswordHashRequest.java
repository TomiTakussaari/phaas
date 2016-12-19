package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PasswordHashRequest {
    @NonNull
    private final String rawPassword;
}
