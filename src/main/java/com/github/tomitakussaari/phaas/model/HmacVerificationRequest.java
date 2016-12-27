package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR;

@Getter
@RequiredArgsConstructor
public class HmacVerificationRequest {
    @NonNull
    private final String message;
    @NonNull
    private final String authenticationCode;

    public int schemeId() {
        return Integer.valueOf(authenticationCode.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[0]);
    }

    public String hmac() {
        return authenticationCode.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
    }


}