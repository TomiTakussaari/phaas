package com.github.tomitakussaari.phaas.user.dao;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class UserConfigurationDTOTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(UserConfigurationDTO.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS)
                .verify();
    }
}
