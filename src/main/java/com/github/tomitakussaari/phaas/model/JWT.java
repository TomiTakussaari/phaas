package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class JWT {

    public enum Algorithm {
        HS256,
        HS384,
        HS512
    }

    @RequiredArgsConstructor
    @Getter
    public static class JwtCreateRequest {
        @NonNull
        private final Algorithm algorithm;
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
