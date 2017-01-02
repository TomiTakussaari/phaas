package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.UsersFromEnvironment.UserData;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationRepository;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import com.github.tomitakussaari.phaas.user.dao.UserRepository;
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
        usersService.createUser("testing-user", PasswordEncodingAlgorithm.ARGON2, Collections.singletonList(UsersService.ROLE.USER), password, sharedSecret);

        System.out.println(UserData.serialize(Collections.singletonList(userData)));
    }
}
