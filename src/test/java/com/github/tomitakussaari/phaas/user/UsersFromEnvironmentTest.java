package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.Collections;

import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.ADMIN;
import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.USER;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UsersFromEnvironmentTest {
    private final Environment environment = Mockito.mock(Environment.class);
    private final ApiUsersService apiUsersService = Mockito.mock(ApiUsersService.class);

    private final String serializedFrom = "[{\"userDTO\":{\"id\":3,\"userName\":\"username\",\"passwordHash\":\"$2a$10$AmFDaMGXu3LHgCivgoli0Op6RmTIWn1xWjF0qy/e5UjvzxGmcGRm6\",\"roles\":\"ROLE_ADMIN, ROLE_USER\"},\"userConfigurationDTOs\":[{\"id\":1,\"user\":\"username\",\"dataProtectionKey\":\"old-data-protection-key\",\"active\":false,\"algorithm\":\"DEFAULT_SHA256ANDBCRYPT\"},{\"id\":2,\"user\":\"username\",\"dataProtectionKey\":\"new-data-protection-key\",\"active\":true,\"algorithm\":\"DEFAULT_SHA256ANDBCRYPT\"}]}]";
    private final UserDTO userDTO = new UserDTO(3, "username", "$2a$10$AmFDaMGXu3LHgCivgoli0Op6RmTIWn1xWjF0qy/e5UjvzxGmcGRm6", "ROLE_ADMIN, ROLE_USER");
    private final UserConfigurationDTO configurationDTO1 = new UserConfigurationDTO(1, userDTO.getUserName(), "old-data-protection-key", false, ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT);
    private final UserConfigurationDTO configurationDTO2 = new UserConfigurationDTO(2, userDTO.getUserName(), "new-data-protection-key", true, ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT);
    private final UsersFromEnvironment.UserData dataFromDTOs = new UsersFromEnvironment.UserData(userDTO, configurationDTO1, configurationDTO2);

    @Test
    public void createsUsersBasedOnEnvironmentConfig() {
        when(environment.getProperty("db.users.content")).thenReturn(serializedFrom);
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(apiUsersService, environment);
        usersFromEnvironment.initializeDatabase();

        verify(apiUsersService).create(userDTO, configurationDTO1, configurationDTO2);
    }

    @Test
    public void userDatabaseSerialization() {
        String serializedData = UsersFromEnvironment.UserData.serialize(Collections.singletonList(dataFromDTOs));
        UsersFromEnvironment.UserData dataFromString = UsersFromEnvironment.UserData.deSerialize(serializedData).get(0);

        assertEquals(serializedFrom, serializedData);
        assertEquals(dataFromDTOs, dataFromString);
    }

    @Test
    public void createsAdminUserIfUserDatabaseIsEmptyAndEnvironmentDoesNotDefineUsers() {
        when(apiUsersService.hasUsers()).thenReturn(false);
        when(apiUsersService.createUser("admin", ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT, asList(ADMIN, USER))).thenReturn("password");
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(apiUsersService, environment);
        usersFromEnvironment.initializeDatabase();
        verify(apiUsersService).createUser("admin", ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT, asList(ADMIN, USER));
    }

    @Test
    public void doesNotCreateAdminUserIfUserDatabaseIsNotEmpty() {
        when(apiUsersService.hasUsers()).thenReturn(true);
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(apiUsersService, environment);
        usersFromEnvironment.initializeDatabase();
        verify(apiUsersService).hasUsers();
        verifyNoMoreInteractions(apiUsersService);
    }
}