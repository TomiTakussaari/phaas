package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;

@UtilityClass
public class JWT {

    @RequiredArgsConstructor
    @Getter
    public static class JwtCreateRequest {
        @NonNull
        private final Map<String, Object> claims;
    }

    @RequiredArgsConstructor
    @Getter
    public static class JwtParseRequest {
        @NonNull
        private final String token;
        @NonNull
        private final Optional<Map<String, Object>> requiredClaims;
    }
}
