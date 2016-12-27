package com.github.tomitakussaari.phaas.user;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT;
import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.ADMIN;
import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.USER;
import static com.github.tomitakussaari.phaas.util.JsonHelper.objectMapSafely;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
class UsersFromEnvironment {
    private final ApiUsersService apiUsersService;
    private final Environment environment;

    @PostConstruct
    void initializeDatabase() {
        String usersConf = environment.getProperty("db.users.content");
        if (usersConf != null) {
            UserData.deSerialize(usersConf).forEach(userData -> apiUsersService.create(userData.getUserDTO(), userData.getUserConfigurationDTOs()));
            log.info("Initialized user database from environment configuration");
        } else if (!apiUsersService.hasUsers()) {
            String signingKey = RandomStringUtils.randomAlphabetic(20);
            String password = apiUsersService.createUser("admin", DEFAULT_SHA256ANDBCRYPT, Arrays.asList(ADMIN, USER), Optional.of(signingKey));
            log.info("|*****| Created 'admin' user with password '{}' and signing key: '{}'", password, signingKey);
        }
    }

    @Data
    @NoArgsConstructor
    static class UserData {
        private static final ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        private static final CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, UserData.class);

        private UserDTO userDTO;
        private UserConfigurationDTO[] userConfigurationDTOs;

        UserData(UserDTO userDTO, UserConfigurationDTO... configurationDTOS) {
            this.userDTO = userDTO;
            this.userConfigurationDTOs = configurationDTOS;
        }

        static List<UserData> deSerialize(String input) {
            return objectMapSafely(() -> objectMapper.readValue(input, collectionType));
        }

        static String serialize(List<UserData> input) {
            return objectMapSafely(() -> objectMapper.writeValueAsString(input));
        }

    }
}
