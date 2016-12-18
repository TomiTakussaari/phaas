package com.github.tomitakussaari.api;

import com.github.tomitakussaari.Application;
import com.github.tomitakussaari.user.ApiUsersService;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.github.tomitakussaari.model.ProtectionScheme.PasswordEncodingAlgorithm.DEFAULT_SHA256ANDBCRYPT;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IT {

    protected static final String USER_NAME = "testuser";
    protected static final String USER_PASSWORD = "testpassword";

    @Autowired
    private ApiUsersService apiUsersService;

    @Value("${local.server.port}")
    int port;

    private final Client authenticatedClient = ClientBuilder.newBuilder().register(JacksonFeature.class).register(HttpBasicAuthFilter.class).build();
    private final Client unAuthenticatedClient = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

    @Before
    public void initialize() {
        apiUsersService.createUser(USER_NAME, DEFAULT_SHA256ANDBCRYPT, Arrays.asList(ApiUsersService.ROLE.ADMIN, ApiUsersService.ROLE.USER), USER_PASSWORD);
    }

    private static class HttpBasicAuthFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle("Authorization", basicAuth(USER_NAME, USER_PASSWORD));
        }
    }

    static String basicAuth(String username, String password) {
        return "Basic " + new String(Base64.encode((username+":"+password).getBytes()), StandardCharsets.US_ASCII);
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
