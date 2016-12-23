package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
import org.apache.commons.codec.digest.HmacUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.USER;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.*;

public class UsersApiIntegrationTest extends IT {

    @Test
    public void findsOutCurrentUser() {
        Map currentUser = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get(Map.class);
        validateUser(currentUser);
    }

    @Test
    public void updatesProtectionScheme() {
        Map currentUserWithOldScheme = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get(Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "DEFAULT_SHA256ANDBCRYPT")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());
        Map currentUserWithNewScheme = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get(Map.class);
        assertNotEquals(currentUserWithOldScheme, currentUserWithNewScheme);
    }

    @Test
    public void returnsResponseSignatureThatCanBeUsedToVerifyResponse() {
        Response response = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get();
        String body = response.readEntity(String.class);
        String actualSignature = response.getHeaderString(SecurityConfig.HmacCalculationAdvice.X_RESPONSE_SIGN);
        String requestId = response.getHeaderString(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID);
        String date = response.getHeaderString("date");
        String expectedSignature = SecurityConfig.HmacCalculationAdvice.calculateSignature(requestId, USER_SIGNING_KEY, date, body);
        assertEquals(expectedSignature, actualSignature);
    }

    private void validateUser(Map currentUser) {
        assertEquals(USER_NAME, currentUser.get("userName"));
        assertThat((Iterable<String>) currentUser.get("roles"), Matchers.containsInAnyOrder("ADMIN", "USER"));
        assertEquals("DEFAULT_SHA256ANDBCRYPT", ((Map) currentUser.get("currentProtectionScheme")).get("algorithm"));
        assertNotNull(((Map) currentUser.get("currentProtectionScheme")).get("algorithm"));
    }

    @Test
    public void canCreateUser() {
        String password = createUserAndReturnPassword("user2", singletonList(USER));

        Map currentUser = unAuthenticatedWebTarget().path("/users/me").request()
                .header("Authorization", basicAuth("user2", password))
                .accept(MediaType.APPLICATION_JSON).get(Map.class);

        assertEquals("user2", currentUser.get("userName"));
    }

    @Test
    public void listsUsers() {
        List users = authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(List.class);
        Map user = (Map) users.get(0);
        validateUser(user);
        assertEquals(1, users.size());
    }

    @Test
    public void canDeleteUser() {
        createUserAndReturnPassword("user2", singletonList(USER));
        assertEquals(2, authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(List.class).size());

        Response delete = authenticatedWebTarget().path("/users/user2").request()
                .accept(MediaType.APPLICATION_JSON).delete();
        assertEquals(200, delete.getStatus());
        assertEquals(1, authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(List.class).size());
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
        assertEquals("Full authentication is required to access this resource", accessDenied.readEntity(Map.class).get("message"));
    }
}
