package com.github.tomitakussaari.phaas.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataProtectionApiIntegrationTest extends IT {

    @Test
    public void createsAndVerifiesJsonWebToken() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), String.class);
        Map response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token)), Map.class);
        assertEquals("value1", response.get("claim1"));
        assertEquals("value2", response.get("claim2"));
    }

    @Test
    public void jsonWebTokenCanBeValidatedAfterPasswordChange() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), String.class);

        Response passwordChangeResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("password", "new-password")));
        assertEquals(200, passwordChangeResponse.getStatus());

        Map response = unAuthenticatedWebTarget().path("/data-protection/token").request()
                .header("Authorization", basicAuth(USER_NAME, "new-password"))
                .put(json(ImmutableMap.of("token", token)), Map.class);
        assertEquals("value1", response.get("claim1"));
        assertEquals("value2", response.get("claim2"));
    }

    @Test
    public void generatesIssuedAtClaimToJsonWebToken() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("a", "b"))), String.class);
        Map response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token)), Map.class);
        assertNotNull(response.get("iat").toString());
    }

    @Test
    public void createsAndVerifiesJsonWebTokenWithWantedClaims() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), String.class);
        authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token, "requiredClaims", ImmutableMap.of("my-claim", "is-true"))), Map.class);
    }

    @Test
    public void jwtWithPreviousProtectionSchemeIsStillValid() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), String.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());
        authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token, "requiredClaims", ImmutableMap.of("my-claim", "is-true"))), Map.class);
    }

    @Test
    public void failsWhenJWTDoesNotContainWantedClaim() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("claim1", "value1"))), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token, "requiredClaims", ImmutableMap.of("my-claim", "is-true"))));
        assertEquals(422, response.getStatus());
        assertEquals("Wrong value for my-claim required: is-true but was null", response.readEntity(Map.class).get("message"));
    }
}