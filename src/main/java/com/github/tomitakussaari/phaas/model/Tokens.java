package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class Tokens {

    @RequiredArgsConstructor
    @Getter
    public static class CreateTokenRequest {
        @NonNull
        private final Map<String, Object> claims;
        @NonNull
        private final Optional<Duration> validityTime;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ParseTokenRequest {
        @NonNull
        private final Optional<Duration> maxAcceptedAge;
        @NonNull
        private final String token;
        @NonNull
        private final Optional<Map<String, Object>> requiredClaims;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ParseTokenResponse {
        @NonNull
        private final Map<String, Object> claims;
        @NonNull
        private final ZonedDateTime issuedAt;
    }
}
