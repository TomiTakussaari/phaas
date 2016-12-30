package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.UsersFromEnvironment.UserData;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationRepository;
import com.github.tomitakussaari.phaas.user.dao.UserRepository;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UsersFromEnvironmentStringCreator {

    private static final UserConfigurationRepository userConfigurationRepository = Mockito.mock(UserConfigurationRepository.class);
    private static final UserRepository userRepository = Mockito.mock(UserRepository.class);

    /**
     * Can be used to print out json configuration for initializing phaas user database
     */
    public static void main(String... args) {
        String sharedSecret = "secret";
        String password = "my-password";
        UsersService usersService = new UsersService(userConfigurationRepository, userRepository);
        final UserData userData = new UserData();
        when(userRepository.save(any(UserDTO.class))).then(invocationOnMock -> {
            userData.setUserDTO((UserDTO) invocationOnMock.getArguments()[0]);
            return invocationOnMock.getArguments()[0];
        });
        when(userConfigurationRepository.save(any(UserConfigurationDTO.class))).then(invocationOnMock -> {
            UserConfigurationDTO userConfigurationDTO = (UserConfigurationDTO) invocationOnMock.getArguments()[0];
            userData.setUserConfigurationDTOs(new UserConfigurationDTO[]{userConfigurationDTO});
            return userConfigurationDTO;
        });
        usersService.createUser("testing-user", PasswordEncodingAlgorithm.SHA256_BCRYPT, Collections.singletonList(UsersService.ROLE.USER), password, sharedSecret);

        System.out.println(UserData.serialize(Collections.singletonList(userData)));
        //db.users.content= [{"userDTO":{"id":null,"userName":"testing-user","passwordHash":"$2a$10$8jH2j2uTf5AEXanJhiFvu.6sS.IDUB25AOqGIeYBwrOlFqe8XAHJm","roles":"ROLE_USER","sharedSecretForSigningCommunication":"secret"},"userConfigurationDTOs":[{"id":null,"user":"testing-user","dataProtectionKey":"da5385256044cfa6.b6c628d3bc92b404c4f7735e52cf80dbd6ad88fbece0a42f564e13d75607d922fd2231d9f309428430dffb1fdb00cf52cd5b080811d866757676509dba50ca77","active":true,"algorithm":"SHA256_BCRYPT"}]}]

    }
}
