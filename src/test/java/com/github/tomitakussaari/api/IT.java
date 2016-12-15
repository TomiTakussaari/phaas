package com.github.tomitakussaari.api;

import com.github.tomitakussaari.Application;
import com.github.tomitakussaari.consumers.ApiConsumerConfigurationRepository;
import com.github.tomitakussaari.consumers.ApiConsumersRepository;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.*;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public abstract class IT {

    @Autowired
    private ApiConsumerConfigurationRepository apiConsumerConfigurationRepository;
    @Autowired
    private ApiConsumersRepository apiConsumersRepository;

    @Value("${local.server.port}")
    int port;

    private final Client authenticatedClient = ClientBuilder.newBuilder().register(JacksonFeature.class).register(HttpBasicAuthFilter.class).build();
    private final Client unAuthenticatedClient = ClientBuilder.newBuilder().register(JacksonFeature.class).register(HttpBasicAuthFilter.class).build();

    @Before
    public void initialize() {
        apiConsumersRepository.save(new ApiConsumersRepository.ApiConsumer("testuser", BCrypt.hashpw("testkey", BCrypt.gensalt()), "ADMIN,USER"));
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.text("testkey", salt);
        String encryptedKey = encryptor.encrypt("encryption_key");
        String saltAndKey = salt + ":::" + encryptedKey;
        apiConsumerConfigurationRepository.save(new ApiConsumerConfigurationRepository.ApiConsumerConfiguration(null, "testuser", saltAndKey, true, "DEFAULT_SHA256ANDBCRYPT"));
    }

    private static class HttpBasicAuthFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle("Authorization", "Basic " + new String(Base64.encode("testuser:testkey".getBytes()), "ASCII"));
        }
    }

    protected WebTarget authenticatedWebTarget() {
        return webTarget(authenticatedClient);
    }

    protected WebTarget unAuthenticatedWebTarget() {
        return webTarget(unAuthenticatedClient);
    }

    protected WebTarget webTarget(Client client) {
        return client.target("http://localhost:" + port);
    }
}
