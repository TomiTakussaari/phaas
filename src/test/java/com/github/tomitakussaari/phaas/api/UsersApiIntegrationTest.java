package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.github.tomitakussaari.phaas.user.UsersService.ROLE.USER;
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
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());
        Map currentUserWithNewScheme = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get(Map.class);
        assertNotEquals(currentUserWithOldScheme, currentUserWithNewScheme);
    }

    @Test
    public void changesPassword() {
        Response updateResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("password", "new-password")));
        assertEquals(200, updateResponse.getStatus());
        Response responseWithOldPassword = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get();
        assertEquals(401, responseWithOldPassword.getStatus());
        Response responseWithNewPassword = unAuthenticatedWebTarget().path("/users/me").request()
                .header("Authorization", basicAuth(USER_NAME, "new-password")).accept(MediaType.APPLICATION_JSON).get();
        assertEquals(200, responseWithNewPassword.getStatus());
    }

    @Test
    public void changesResponseSigningKey() {
        Response updateResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("password", USER_PASSWORD, "sharedSecretForSigningCommunication", "sign-key")));
        assertEquals(200, updateResponse.getStatus());
        Response response = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get();
        validateResponseSignature(response, "sign-key");
    }

    @Test
    public void returnsResponseSignatureThatCanBeUsedToVerifyResponse() {
        Response response = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get();
        validateResponseSignature(response, USER_SIGNING_KEY);
    }

    private void validateResponseSignature(Response response, String signKey) {
        String body = response.readEntity(String.class);
        String actualSignature = response.getHeaderString(SecurityConfig.HmacCalculationAdvice.X_RESPONSE_SIGN);
        String requestId = response.getHeaderString(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID);
        String date = response.getHeaderString("date");
        String expectedSignature = SecurityConfig.HmacCalculationAdvice.calculateSignature(requestId, signKey, date, body);
        assertEquals(expectedSignature, actualSignature);
    }

    private void validateUser(Map currentUser) {
        assertEquals(USER_NAME, currentUser.get("userName"));
        assertThat((Iterable<String>) currentUser.get("roles"), Matchers.containsInAnyOrder("ADMIN", "USER"));
        assertEquals("SHA256_BCRYPT", ((Map) currentUser.get("currentProtectionScheme")).get("algorithm"));
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

        Response accessDenied = unAuthenticatedWebTarget().path("/users").request().header("Authorization", basicAuth("user3", password)).post(json(of("userName", "user4", "algorithm", "SHA256_BCRYPT", "roles", singletonList(USER))));
        assertEquals(401, accessDenied.getStatus());
        assertEquals("No access", accessDenied.readEntity(Map.class).get("message"));
    }

    @Test
    public void unAuthenticatedCannotCreateNewUsers() {
        Response accessDenied = unAuthenticatedWebTarget().path("/users").request().post(json(of("userName", "user4", "algorithm", "SHA256_BCRYPT", "roles", singletonList(USER))));
        assertEquals(401, accessDenied.getStatus());
        assertEquals("Full authentication is required to access this resource", accessDenied.readEntity(Map.class).get("message"));
    }
}
