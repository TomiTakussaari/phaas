package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.UsersService;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.when;

public class UsersApiTest {

    private final UsersService usersService = Mockito.mock(UsersService.class);
    private final Environment environment = Mockito.mock(Environment.class);
    private final UsersApi usersApi = new UsersApi(usersService, environment);

    @Test
    public void equalsContractForNewPasswordRequest() {
        EqualsVerifier.forClass(UsersApi.NewPasswordRequest.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void equalsContractForPublicUser() {
        EqualsVerifier.forClass(UsersApi.PublicUser.class)
                .suppress(Warning.NONFINAL_FIELDS, Warning.NULL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test(expected = OperationIsNotAvailableException.class)
    public void newUserCreationCanBePreventedWithConfiguration() {
        when(environment.getProperty("immutable.users.db", Boolean.class)).thenReturn(true);
        usersApi.createUser(null);
    }

    @Test(expected = OperationIsNotAvailableException.class)
    public void userDeletionCanBePreventedWithConfiguration() {
        when(environment.getProperty("immutable.users.db", Boolean.class)).thenReturn(true);
        usersApi.deleteUser("");
    }

    @Test(expected = OperationIsNotAvailableException.class)
    public void schemeChangeCanBePreventedWithConfiguration() {
        when(environment.getProperty("immutable.users.db", Boolean.class)).thenReturn(true);
        usersApi.newProtectionScheme(null, null);
    }

    @Test(expected = OperationIsNotAvailableException.class)
    public void passwordChangeCanBePreventedWithConfiguration() {
        when(environment.getProperty("immutable.users.db", Boolean.class)).thenReturn(true);
        usersApi.changePassword(null, null);
    }

}
