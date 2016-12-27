package com.github.tomitakussaari.phaas.model;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

public class JWT {

    @RequiredArgsConstructor
    @Getter
    public static class JwtCreateRequest {
        @NonNull
        private final SignatureAlgorithm algorithm;
        @NonNull
        private final Map<String, Object> claims;
    }

    @RequiredArgsConstructor
    @Getter
    public static class JwtParseRequest {
        @NonNull
        private final String token;
        private final Map<String, Object> requiredClaims;
    }
}
