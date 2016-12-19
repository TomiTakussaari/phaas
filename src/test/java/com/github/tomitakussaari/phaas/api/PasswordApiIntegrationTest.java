package com.github.tomitakussaari.phaas.api;

import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordApiIntegrationTest extends IT {

    private static final String PASSWORD = "my-secret-password";

    @Test
    public void hashesAndVerifiesPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD)), Map.class);
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash"))), Map.class);
        assertTrue((boolean) verifyResponse.get("valid"));
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
        Response response = unAuthenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", "wrong-password", "hash", "1:::foobar")));
        assertEquals(401, response.getStatus());
    }
}
