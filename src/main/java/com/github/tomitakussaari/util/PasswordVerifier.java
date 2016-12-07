package com.github.tomitakussaari.util;

import com.github.tomitakussaari.model.PasswordVerifyRequest;
import com.github.tomitakussaari.model.PasswordVerifyResult;

import java.util.Optional;

public class PasswordVerifier {

    public PasswordVerifyResult verify(PasswordVerifyRequest request, String pepper) {
        return new PasswordVerifyResult(Optional.empty(), false);
    }
}
