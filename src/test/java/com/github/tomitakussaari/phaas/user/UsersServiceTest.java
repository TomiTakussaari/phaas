package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.user.UsersService.ROLE;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class UsersServiceTest {

    @Test
    public void roleParsingFailsForInvalidValue() {
        try {
            ROLE.fromDbValue("NULL");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("No enum constant com.github.tomitakussaari.phaas.user.UsersService.ROLE.NULL");
        }
    }

    @Test
    public void roleParsingWorksForBothEnumNameAndDbValue() {
        assertThat(ROLE.fromDbValue(UsersService.ADMIN_ROLE_VALUE)).isEqualTo(ROLE.ADMIN);
        assertThat(ROLE.fromDbValue(ROLE.ADMIN.name())).isEqualTo(ROLE.ADMIN);
    }

}