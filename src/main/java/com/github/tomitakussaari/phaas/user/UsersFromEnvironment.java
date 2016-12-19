package com.github.tomitakussaari.phaas.user;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.tomitakussaari.phaas.user.PhaasUserConfigurationRepository.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.PhaasUserRepository.UserDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
class UsersFromEnvironment {
    private final ApiUsersService apiUsersService;
    private final Environment environment;

    @PostConstruct
    void initializeDatabase() {
        String usersConf = environment.getProperty("db.users.content");
        if(usersConf != null) {
            UserData.deSerialize(usersConf).forEach(userData -> apiUsersService.create(userData.getUserDTO(), userData.getUserConfigurationDTOs()));
        }
    }

    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor
    static class UserData {
        private static final ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        private static final CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, UserData.class);

        private UserDTO userDTO;
        private UserConfigurationDTO[] userConfigurationDTOs;

        UserData(UserDTO userDTO, UserConfigurationDTO...configurationDTOS) {
            this.userDTO = userDTO;
            this.userConfigurationDTOs = configurationDTOS;
        }

        static List<UserData> deSerialize(String input) {
            try {
                return objectMapper.readValue(input, collectionType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        static String serialize(List<UserData> input) {
            try {
                return objectMapper.writeValueAsString(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
