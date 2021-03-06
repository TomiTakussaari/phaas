package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import com.github.tomitakussaari.phaas.util.JsonHelper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.github.tomitakussaari.phaas.user.UsersService.ROLE.ADMIN;
import static com.github.tomitakussaari.phaas.user.UsersService.ROLE.USER;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UsersFromEnvironmentTest {
    private final Environment environment = Mockito.mock(Environment.class);
    private final UsersService usersService = Mockito.mock(UsersService.class);

    private final String serializedFrom = "W3sidXNlckRUTyI6eyJpZCI6MywidXNlck5hbWUiOiJ1c2VybmFtZSIsInJvbGVzIjoiUk9MRV9BRE1JTiwgUk9MRV9VU0VSIiwic2hhcmVkU2VjcmV0Rm9yU2lnbmluZ0NvbW11bmljYXRpb24iOiJteS1zaGFyZWQtc2VjcmV0In0sInVzZXJDb25maWd1cmF0aW9uRFRPcyI6W3siaWQiOjEsInVzZXIiOiJ1c2VybmFtZSIsImRhdGFQcm90ZWN0aW9uS2V5Ijoib2xkLWRhdGEtcHJvdGVjdGlvbi1rZXkiLCJhY3RpdmUiOmZhbHNlLCJhbGdvcml0aG0iOiJTSEEyNTZfQkNSWVBUIn0seyJpZCI6MiwidXNlciI6InVzZXJuYW1lIiwiZGF0YVByb3RlY3Rpb25LZXkiOiJuZXctZGF0YS1wcm90ZWN0aW9uLWtleSIsImFjdGl2ZSI6dHJ1ZSwiYWxnb3JpdGhtIjoiU0hBMjU2X0JDUllQVCJ9XX1d";
    private final UserDTO userDTO = new UserDTO(3, "username", "ROLE_ADMIN, ROLE_USER", "my-shared-secret");
    private final UserConfigurationDTO configurationDTO1 = new UserConfigurationDTO(1, userDTO.getUserName(), "old-data-protection-key", false, PasswordEncodingAlgorithm.SHA256_BCRYPT);
    private final UserConfigurationDTO configurationDTO2 = new UserConfigurationDTO(2, userDTO.getUserName(), "new-data-protection-key", true, PasswordEncodingAlgorithm.SHA256_BCRYPT);
    private final UsersFromEnvironment.UserData dataFromDTOs = new UsersFromEnvironment.UserData(userDTO, configurationDTO1, configurationDTO2);

    @Test
    public void createsUsersBasedOnEnvironmentConfig() {
        when(environment.getProperty("db.users.content")).thenReturn(serializedFrom);
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(usersService, environment);
        usersFromEnvironment.initializeDatabase();

        verify(usersService).save(refEq(userDTO), refEq(configurationDTO1), refEq(configurationDTO2));
    }

    @Test
    public void userDatabaseSerialization() {
        String serializedData = UsersFromEnvironment.UserData.serialize(Collections.singletonList(dataFromDTOs));
        UsersFromEnvironment.UserData dataFromString = UsersFromEnvironment.UserData.deSerialize(serializedData).get(0);

        assertThat(serializedData).isEqualTo(serializedFrom);
        assertThat(dataFromString).isEqualToComparingFieldByFieldRecursively(dataFromDTOs);
    }

    @Test
    public void createsAdminUserIfUserDatabaseIsEmptyAndEnvironmentDoesNotDefineUsers() {
        when(usersService.hasUsers()).thenReturn(false);
        when(usersService.createUser(eq("admin"), eq(PasswordEncodingAlgorithm.SHA256_BCRYPT), eq(asList(ADMIN, USER)), argThat(new OptionaIsPresentMatcher<>()))).thenReturn("password");
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(usersService, environment);
        usersFromEnvironment.initializeDatabase();
        verify(usersService).createUser(eq("admin"), eq(PasswordEncodingAlgorithm.SHA256_BCRYPT), eq(asList(ADMIN, USER)), argThat(new OptionaIsPresentMatcher<>()));
    }

    @Test
    public void doesNotCreateAdminUserIfUserDatabaseIsNotEmpty() {
        when(usersService.hasUsers()).thenReturn(true);
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(usersService, environment);
        usersFromEnvironment.initializeDatabase();
        verify(usersService).hasUsers();
        verifyNoMoreInteractions(usersService);
    }

    @Test(expected = RuntimeException.class)
    public void userDataObjectMapSafelyThrowsRuntimeExceptionOnIOException() {
        JsonHelper.objectMapSafely(() -> {
            throw new IOException("expected");
        });
    }

    static class OptionaIsPresentMatcher<T> extends BaseMatcher<Optional<T>> implements Matcher<Optional<T>> {

        @Override
        public boolean matches(Object o) {
            return o instanceof Optional && Optional.class.cast(o).isPresent();
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
