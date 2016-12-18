package com.github.tomitakussaari.user;

import com.github.tomitakussaari.model.ProtectionScheme.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.user.PhaasUserConfigurationRepository.UserConfigurationDTO;
import com.github.tomitakussaari.user.PhaasUserRepository.UserDTO;
import com.github.tomitakussaari.user.UsersFromEnvironment.UserData;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.Collections;

import static org.junit.Assert.*;

public class UsersFromEnvironmentTest {

    private final String serializedFrom = "[{\"userDTO\":{\"userName\":\"username\",\"passwordHash\":\"$2a$10$AmFDaMGXu3LHgCivgoli0Op6RmTIWn1xWjF0qy/e5UjvzxGmcGRm6\",\"roles\":\"ROLE_ADMIN, ROLE_USER\"},\"userConfigurationDTOs\":[{\"id\":1,\"userName\":\"username\",\"dataProtectionKey\":\"old-data-protection-key\",\"active\":false,\"algorithm\":\"DEFAULT_SHA256ANDBCRYPT\"},{\"id\":2,\"userName\":\"username\",\"dataProtectionKey\":\"new-data-protection-key\",\"active\":true,\"algorithm\":\"DEFAULT_SHA256ANDBCRYPT\"}]}]";
    private final UserDTO userDTO = new UserDTO("username", "$2a$10$AmFDaMGXu3LHgCivgoli0Op6RmTIWn1xWjF0qy/e5UjvzxGmcGRm6", "ROLE_ADMIN, ROLE_USER");
    private final UserConfigurationDTO configurationDTO1 = new UserConfigurationDTO(1, "username", "old-data-protection-key", false, PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT);
    private final UserConfigurationDTO configurationDTO2 = new UserConfigurationDTO(2, "username", "new-data-protection-key", true, PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT);
    private final UserData dataFromDTOs = new UserData(userDTO, configurationDTO1, configurationDTO2);

    @Test
    public void createsUsersBasedOnEnvironmentConfig() {
        Environment environment = Mockito.mock(Environment.class);
        ApiUsersService apiUsersService = Mockito.mock(ApiUsersService.class);
        Mockito.when(environment.getProperty("db.users.content")).thenReturn(serializedFrom);
        UsersFromEnvironment usersFromEnvironment = new UsersFromEnvironment(apiUsersService, environment);
        usersFromEnvironment.initializeDatabase();

        Mockito.verify(apiUsersService).create(userDTO, configurationDTO1, configurationDTO2);
    }

    @Test
    public void userDatabaseSerialization() {
        String serializedData = UserData.serialize(Collections.singletonList(dataFromDTOs));
        UserData dataFromString = UserData.deSerialize(serializedData).get(0);

        assertEquals(serializedFrom, serializedData);
        assertEquals(dataFromDTOs, dataFromString);

    }
}