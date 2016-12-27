package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.UsersFromEnvironment.UserData;
import com.github.tomitakussaari.phaas.user.dao.PhaasUserConfigurationRepository;
import com.github.tomitakussaari.phaas.user.dao.PhaasUserRepository;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UsersFromEnvironmentStringCreator {

    private static final PhaasUserConfigurationRepository userConfigurationRepository = Mockito.mock(PhaasUserConfigurationRepository.class);
    private static final PhaasUserRepository userRepository = Mockito.mock(PhaasUserRepository.class);

    /**
     * Can be used to print out json configuration for initializing phaas user database
     */
    public static void main(String... args) {
        String sharedSecret = "secret";
        String password = "my-password";
        ApiUsersService apiUsersService = new ApiUsersService(userConfigurationRepository, userRepository);
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
        apiUsersService.createUser("testing-user", PasswordEncodingAlgorithm.SHA256_BCRYPT, Collections.singletonList(ApiUsersService.ROLE.USER), password, sharedSecret);

        System.out.println(UserData.serialize(Collections.singletonList(userData)));
        //db.users.content= [{"userDTO":{"id":null,"userName":"testing-user","passwordHash":"$2a$10$BRHdJNvWFTXEZUqmgOrrWOgjAQLfEmojjeVvzsf.PrNEWCPs.4CQq","roles":"ROLE_USER","sharedSecretForSigningCommunication":"secret"},"userConfigurationDTOs":[{"id":null,"user":"testing-user","dataProtectionKey":"2a0be8ce62026ccc.3b650d5231ec2ad6ffa02b00b64c24a51fe59dbc463613dcc8805e78a1ec0b34855600e8f91f301a2259277d0d12091ccd0400027eff25e33887f4e547e5c1e4051540cb39019db8571ba55ef6f0d32bc357731b41e3e70a2c6f699e9473803dc7d465205447fac9563c3165aaf85f47","active":true,"algorithm":"SHA256_BCRYPT"}]}]

    }
}
