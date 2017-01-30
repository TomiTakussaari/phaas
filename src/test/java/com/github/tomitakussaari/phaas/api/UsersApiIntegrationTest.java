package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
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
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(updateResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        Map currentUserWithNewScheme = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get(Map.class);
        assertThat(currentUserWithNewScheme).isNotEqualTo(currentUserWithOldScheme);
    }

    @Test
    public void changesPassword() {
        Response updateResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("newPassword", "new-password")));
        assertThat(updateResponse.getStatus()).isEqualTo(200);
        Response responseWithOldPassword = authenticatedWebTarget().path("/users/me").request().accept(MediaType.APPLICATION_JSON).get();
        assertThat(responseWithOldPassword.getStatus()).isEqualTo(401);
        Response responseWithNewPassword = unAuthenticatedWebTarget().path("/users/me").request()
                .header("Authorization", basicAuth(USER_NAME, "new-password")).accept(MediaType.APPLICATION_JSON).get();
        assertThat(responseWithNewPassword.getStatus()).isEqualTo(200);
    }

    @Test
    public void changesResponseSigningKey() {
        Response updateResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("newPassword", USER_PASSWORD, "sharedSecretForSigningCommunication", "sign-key")));
        assertThat(updateResponse.getStatus()).isEqualTo(200);
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
        assertThat(actualSignature).isEqualTo(expectedSignature);
    }

    private void validateUser(Map currentUser) {
        assertThat(currentUser.get("userName")).isEqualTo(USER_NAME);
        assertThat((Iterable<String>) currentUser.get("roles")).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(((Map) currentUser.get("currentProtectionScheme")).get("algorithm")).isEqualTo("SHA256_BCRYPT");
        assertThat(((Map) currentUser.get("currentProtectionScheme")).get("algorithm")).isNotNull();
    }

    @Test
    public void canCreateUser() {
        String password = createUserAndReturnPassword("user2", singletonList(USER));

        Map currentUser = unAuthenticatedWebTarget().path("/users/me").request()
                .header("Authorization", basicAuth("user2", password))
                .accept(MediaType.APPLICATION_JSON).get(Map.class);

        assertThat(currentUser.get("userName")).isEqualTo("user2");
    }

    @Test
    public void listsUsers() {
        List users = authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(List.class);
        Map user = (Map) users.get(0);
        validateUser(user);
        assertThat(users).hasSize(1);
    }

    @Test
    public void canDeleteUser() {
        createUserAndReturnPassword("user2", singletonList(USER));
        assertThat(authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(List.class)).hasSize(2);

        Response delete = authenticatedWebTarget().path("/users/user2").request()
                .accept(MediaType.APPLICATION_JSON).delete();
        assertThat(delete.getStatus()).isEqualTo(200);
        assertThat(authenticatedWebTarget().path("/users").request().accept(MediaType.APPLICATION_JSON).get(List.class)).hasSize(1);
    }

    @Test
    public void nonAdminUserCannotCreateNewUsers() {
        String password = createUserAndReturnPassword("user3", singletonList(USER));

        Response accessDenied = unAuthenticatedWebTarget().path("/users").request().header("Authorization", basicAuth("user3", password)).post(json(of("userName", "user4", "algorithm", "SHA256_BCRYPT", "roles", singletonList(USER))));
        assertThat(accessDenied.getStatus()).isEqualTo(401);
        assertThat(accessDenied.readEntity(Map.class).get("message")).isEqualTo("No access");
    }

    @Test
    public void unAuthenticatedCannotCreateNewUsers() {
        Response accessDenied = unAuthenticatedWebTarget().path("/users").request().post(json(of("userName", "user4", "algorithm", "SHA256_BCRYPT", "roles", singletonList(USER))));
        assertThat(accessDenied.getStatus()).isEqualTo(401);
        assertThat(accessDenied.readEntity(Map.class).get("message")).isEqualTo("Full authentication is required to access this resource");
    }
}
