package com.github.tomitakussaari.phaas.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class DataProtectionSchemeTest {

    @Test
    public void equalsContractOfDataProtectionScheme() {
        EqualsVerifier.forClass(DataProtectionScheme.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void equalsContractOfCryptoData() {
        EqualsVerifier.forClass(DataProtectionScheme.CryptoData.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void equalsContractOfPublicProtectionScheme() {
        EqualsVerifier.forClass(DataProtectionScheme.PublicProtectionScheme.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }
}