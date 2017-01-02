package com.github.tomitakussaari.phaas.api;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.tomitakussaari.phaas.user.UsersService.ROLE.USER;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;

public class PasswordApiIntegrationTest extends IT {

    private static final String PASSWORD = "my-secret-password";

    @Test
    public void hashesAndVerifiesPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash"))), Map.class);
        assertThat((boolean) verifyResponse.get("valid")).isTrue();
    }

    @Test
    public void cannotVerifyPasswordProtectedByOtherUser() {
        String password = createUserAndReturnPassword("otheruser", singletonList(USER));
        Map hashedPassword = unAuthenticatedWebTarget().path("/passwords/hash").request().header("Authorization", basicAuth("otheruser", password)).put(json(of("rawPassword", PASSWORD)), Map.class);

        Response response = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash"))));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(Map.class).get("message")).isEqualTo("DataProtectionScheme was not found");
    }

    @Test
    public void passwordWithOldSchemeCanBeVerifiedAfterUpdatingScheme() {
        Map hashedPasswordWithOldScheme = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT")));
        assertThat(updateResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);

        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPasswordWithOldScheme.get("hash"))), Map.class);
        assertThat((boolean) verifyResponse.get("valid")).isTrue();
    }

    @Test
    public void passwordChangeDoesNotInvalidateOldPasswordHashes() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);

        Response passwordChangeResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("password", "new-password")));
        assertThat(passwordChangeResponse.getStatus()).isEqualTo(200);

        Response response = unAuthenticatedWebTarget().path("/passwords/verify").request()
                .header("Authorization", basicAuth(USER_NAME, "new-password"))
                .put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash"))));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat((boolean) response.readEntity(Map.class).get("valid")).isTrue();
    }

    @Test
    public void passwordWithOldSchemeCannotBeVerifiedAfterUpdatingSchemeWithInvalidateOldFlagTrue() {
        Map hashedPasswordWithOldScheme = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT", "removeOldSchemes", "true")));
        assertThat(updateResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);

        Response response = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPasswordWithOldScheme.get("hash"))));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(Map.class).get("message")).isEqualTo("DataProtectionScheme was not found");
    }

    @Test
    public void passwordVerificationReturnsUpgradePasswordHashAfterUpdatingScheme() {
        Map hashedPasswordWithOldScheme = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT")));
        assertThat(updateResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);

        Map verifyResponseWithUpgradedHash = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPasswordWithOldScheme.get("hash"))), Map.class);
        assertThat(verifyResponseWithUpgradedHash.get("upgradedHash")).isNotNull();
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", verifyResponseWithUpgradedHash.get("upgradedHash"))), Map.class);
        assertThat((boolean) verifyResponse.get("valid")).isTrue();
        assertThat(verifyResponse.get("upgradedHash")).isNull();
    }

    @Test
    public void noticesInvalidPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response response = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", "wrong-password", "hash", hashedPassword.get("hash"))));
        assertThat(response.getStatus()).isEqualTo(422);
        Map verifyResponse = response.readEntity(Map.class);
        assertThat((boolean) verifyResponse.get("valid")).isFalse();
    }

    @Test
    public void unAuthenticatedCannotAccessHashingApi() {
        Response response = unAuthenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)));
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void unAuthenticatedCannotAccessVerifyApi() {
        Response response = unAuthenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", "wrong-password", "hash", "1.foobar")));
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
