package com.github.tomitakussaari.phaas.user;

import org.junit.Test;

public class PhaasUserTest {

    @Test(expected = UnsupportedOperationException.class)
    public void cannotGetPassword() {
        PhaasUser phaasUser = new PhaasUser(null, null, null);
        phaasUser.getPassword();
    }

}