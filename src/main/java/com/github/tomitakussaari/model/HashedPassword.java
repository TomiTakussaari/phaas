package com.github.tomitakussaari.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class HashedPassword {
    private final String hash;
}
