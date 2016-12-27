package com.github.tomitakussaari.phaas.api;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.tomitakussaari.phaas.user.ApiUsersService.ROLE.USER;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.*;

public class PasswordApiIntegrationTest extends IT {

    private static final String PASSWORD = "my-secret-password";

    @Test
    public void hashesAndVerifiesPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash"))), Map.class);
        assertTrue((boolean) verifyResponse.get("valid"));
    }

    @Test
    public void cannotVerifyPassowrdProtectedByOtherUser() {
        String password = createUserAndReturnPassword("otheruser", singletonList(USER));
        Map hashedPassword = unAuthenticatedWebTarget().path("/passwords/hash").request().header("Authorization", basicAuth("otheruser", password)).put(json(of("rawPassword", PASSWORD)), Map.class);

        Response response = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash"))));
        assertEquals(400, response.getStatus());
        assertEquals("DataProtectionScheme was not found", response.readEntity(Map.class).get("message"));
    }

    @Test
    public void passwordWithOldSchemeCanBeVerifiedAfterUpdatingScheme() {
        Map hashedPasswordWithOldScheme = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "DEFAULT_SHA256ANDBCRYPT")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());

        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPasswordWithOldScheme.get("hash"))), Map.class);
        assertTrue((boolean) verifyResponse.get("valid"));
    }

    @Test
    public void passwordWithOldSchemeCannotBeVerifiedAfterUpdatingSchemeWithInvalidateOldFlagTrue() {
        Map hashedPasswordWithOldScheme = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "DEFAULT_SHA256ANDBCRYPT", "removeOldSchemes", "true")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());

        Response response = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPasswordWithOldScheme.get("hash"))));
        assertEquals(400, response.getStatus());
        assertEquals("DataProtectionScheme was not found", response.readEntity(Map.class).get("message"));
    }

    @Test
    public void passwordVerificationReturnsUpgradePasswordHashAfterUpdatingScheme() {
        Map hashedPasswordWithOldScheme = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "DEFAULT_SHA256ANDBCRYPT")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());

        Map verifyResponseWithUpgradedHash = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPasswordWithOldScheme.get("hash"))), Map.class);
        assertNotNull(verifyResponseWithUpgradedHash.get("upgradedHash"));
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", verifyResponseWithUpgradedHash.get("upgradedHash"))), Map.class);
        assertTrue((boolean) verifyResponse.get("valid"));
        assertNull(verifyResponse.get("upgradedHash"));
    }

    @Test
    public void noticesInvalidPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", "wrong-password", "hash", hashedPassword.get("hash"))), Map.class);
        assertFalse((boolean) verifyResponse.get("valid"));
    }

    @Test
    public void unAuthenticatedCannotAccessHashingApi() {
        Response response = unAuthenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)));
        assertEquals(401, response.getStatus());
    }

    @Test
    public void unAuthenticatedCannotAccessVerifyApi() {
        Response response = unAuthenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", "wrong-password", "hash", "1.foobar")));
        assertEquals(401, response.getStatus());
    }
}
