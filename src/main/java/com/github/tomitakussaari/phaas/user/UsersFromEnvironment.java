package com.github.tomitakussaari.phaas.user;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm.SHA256_BCRYPT;
import static com.github.tomitakussaari.phaas.user.UsersService.ROLE.ADMIN;
import static com.github.tomitakussaari.phaas.user.UsersService.ROLE.USER;
import static com.github.tomitakussaari.phaas.util.JsonHelper.objectMapSafely;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
class UsersFromEnvironment {
    private final UsersService usersService;
    private final Environment environment;

    @PostConstruct
    void initializeDatabase() {
        String usersConf = environment.getProperty("db.users.content");
        if (usersConf != null) {
            UserData.deSerialize(usersConf).forEach(userData -> usersService.save(userData.getUserDTO(), userData.getUserConfigurationDTOs()));
            log.info("Initialized user database from environment configuration");
        } else if (!usersService.hasUsers()) {
            String signingKey = RandomStringUtils.randomAlphabetic(20);
            String password = usersService.createUser("admin", SHA256_BCRYPT, Arrays.asList(ADMIN, USER), Optional.of(signingKey));
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
            String data = new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
            return objectMapSafely(() -> objectMapper.readValue(data, collectionType));
        }

        static String serialize(List<UserData> input) {
            String data = objectMapSafely(() -> objectMapper.writeValueAsString(input));
            return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        }

    }
}
