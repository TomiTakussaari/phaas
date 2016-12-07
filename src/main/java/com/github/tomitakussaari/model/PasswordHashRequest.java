package com.github.tomitakussaari.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PasswordHashRequest {
    @NonNull
    private final String rawPassword;
}
