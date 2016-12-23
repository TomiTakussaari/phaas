package com.github.tomitakussaari.phaas.user.dao;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class UserDTOTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(UserDTO.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS)
                .verify();
    }

}