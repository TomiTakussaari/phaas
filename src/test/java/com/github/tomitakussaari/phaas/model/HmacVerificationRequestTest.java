package com.github.tomitakussaari.phaas.model;

import org.junit.Test;

public class HmacVerificationRequestTest {

    @Test(expected = NullPointerException.class)
    public void cannotConstructWithoutAuthenticationCode() {
        new HmacVerificationRequest("", null);
    }

    @Test(expected = NullPointerException.class)
    public void cannotConstructWithoutMessage() {
        new HmacVerificationRequest(null, "");
    }

}