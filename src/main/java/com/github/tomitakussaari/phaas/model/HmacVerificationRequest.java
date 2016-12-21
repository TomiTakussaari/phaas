package com.github.tomitakussaari.phaas.model;

import com.github.tomitakussaari.phaas.api.DataProtectionApi;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HmacVerificationRequest {
    @NonNull
    private final String message;
    @NonNull
    private final String authenticationCode;

    public int schemeId() {
        return Integer.valueOf(authenticationCode.split(DataProtectionApi.HASH_VALUE_SEPARATOR)[0]);
    }

    public String hmac() {
        return authenticationCode.split(DataProtectionApi.HASH_VALUE_SEPARATOR)[1];
    }


}