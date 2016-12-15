package com.github.tomitakussaari.api;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.assertEquals;

public class PasswordApiIntegrationTest extends IT {

    private static final String PASSWORD = "my-secret-password";

    @Test
    public void hashesAndVerifiesPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(json(of("rawPassword", PASSWORD))).readEntity(Map.class);
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(json(of("passwordCandidate", PASSWORD, "hash", hashedPassword.get("hash")))).readEntity(Map.class);
        assertEquals(true, verifyResponse.get("valid"));
    }
}
