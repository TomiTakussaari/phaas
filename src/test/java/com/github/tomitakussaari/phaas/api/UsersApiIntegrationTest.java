package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.ApiUsersService;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.USER;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class UsersApiIntegrationTest extends IT {

    @Test
    public void findsOutCurrentUser() {
        Map currentUser = authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(Map.class);
        assertEquals(USER_NAME, currentUser.get("userName"));
        assertThat((Iterable<String>) currentUser.get("roles"), Matchers.containsInAnyOrder("ADMIN", "USER"));
        assertEquals("DEFAULT_SHA256ANDBCRYPT", ((Map) currentUser.get("currentProtectionScheme")).get("algorithm"));
        assertNotNull(((Map) currentUser.get("currentProtectionScheme")).get("algorithm"));
    }

    @Test
    public void canCreateUser() {
        String password = createUserAndReturnPassword("user2", singletonList(USER));

        Map currentUser = unAuthenticatedWebTarget().path("/users").request()
                .header("Authorization", basicAuth("user2", password))
                .accept(MediaType.APPLICATION_JSON).get(Map.class);

        assertEquals("user2", currentUser.get("userName"));
    }

    @Test
    public void nonAdminUserCannotCreateNewUsers() {
        String password = createUserAndReturnPassword("user3", singletonList(USER));

        Response accessDenied = unAuthenticatedWebTarget().path("/users").request().header("Authorization", basicAuth("user3", password)).post(json(of("userName", "user4", "algorithm", "DEFAULT_SHA256ANDBCRYPT", "roles", singletonList(USER))));
        assertEquals(401, accessDenied.getStatus());
        assertEquals("No access", accessDenied.readEntity(Map.class).get("message"));
    }

    @Test
    public void unAuthenticatedCannotCreateNewUsers() {
        Response accessDenied = unAuthenticatedWebTarget().path("/users").request().post(json(of("userName", "user4", "algorithm", "DEFAULT_SHA256ANDBCRYPT", "roles", singletonList(USER))));
        assertEquals(401, accessDenied.getStatus());
        assertEquals("No access", accessDenied.readEntity(Map.class).get("message"));
    }

    private String createUserAndReturnPassword(String username, List<ApiUsersService.ROLE> roles) {
        Map newUserResponse = authenticatedWebTarget().path("/users").request()
                .post(json(of("userName", username, "algorithm", "DEFAULT_SHA256ANDBCRYPT", "roles", roles)), Map.class);
        return (String) newUserResponse.get("generatedPassword");
    }
}
