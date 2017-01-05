package com.github.tomitakussaari.phaas.model;

import com.github.tomitakussaari.phaas.util.CryptoHelper;
import com.github.tomitakussaari.phaas.util.PepperSource;
import org.junit.Test;

import static com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm.ARGON2;

public class DataProtectionSchemeTest {

    @Test(expected = NullPointerException.class)
    public void refusesToBeCreatedWithNullCryptoHelper() {
        new DataProtectionScheme(1, ARGON2, "", null);
    }

    @Test(expected = NullPointerException.class)
    public void refusesToBeCreatedWithNullKey() {
        new DataProtectionScheme(1, ARGON2, null, new CryptoHelper(new PepperSource("")));
    }

    @Test(expected = NullPointerException.class)
    public void refusesToBeCreatedWithNullAlgorithm() {
        new DataProtectionScheme(1, null, "", new CryptoHelper(new PepperSource("")));
    }

}