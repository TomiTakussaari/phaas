package com.github.tomitakussaari.phaas.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.github.tomitakussaari.phaas.Application;
import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.UsersService;
import com.github.tomitakussaari.phaas.user.dao.UserRepository;
import com.github.tomitakussaari.phaas.util.JsonHelper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IT {

    protected static final String USER_NAME = "testuser";
    protected static final String USER_PASSWORD = "testpassword";
    protected static final String USER_SIGNING_KEY = "test-sign-key";

    @Autowired
    protected UsersService usersService;
    @Autowired
    protected UserRepository userRepository;

    @Value("${local.server.port}")
    int port;

    private final Client authenticatedClient;
    private final Client unAuthenticatedClient;

    public IT() {
        authenticatedClient = ClientBuilder.newBuilder().register(ObjectMapperContextResolver.class).register(JacksonFeature.class).register(HttpBasicAuthFilter.class).build();
        unAuthenticatedClient = ClientBuilder.newBuilder().register(ObjectMapperContextResolver.class).register(JacksonFeature.class).build();
    }

    @Provider
    static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return JsonHelper.objectMapper;
        }
    }

    @Before
    public void initialize() {
        usersService.createUser(USER_NAME, PasswordEncodingAlgorithm.SHA256_BCRYPT, Arrays.asList(UsersService.ROLE.ADMIN, UsersService.ROLE.USER), USER_PASSWORD, USER_SIGNING_KEY);
    }

    @After
    public void clearDb() {
        usersService.findAll().forEach(user -> usersService.deleteUser(user.getUsername()));
    }

    private static class HttpBasicAuthFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle("Authorization", basicAuth(USER_NAME, USER_PASSWORD));
        }
    }

    static String basicAuth(String username, String password) {
        return "Basic " + new String(Base64.encode((username + ":" + password).getBytes()), StandardCharsets.US_ASCII);
    }

    protected String createUserAndReturnPassword(String username, List<UsersService.ROLE> roles) {
        Map newUserResponse = authenticatedWebTarget().path("/users").request()
                .post(json(of("userName", username, "algorithm", "SHA256_BCRYPT", "roles", roles)), Map.class);
        return (String) newUserResponse.get("generatedPassword");
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
