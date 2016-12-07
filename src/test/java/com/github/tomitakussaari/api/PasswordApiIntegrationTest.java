package com.github.tomitakussaari.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PasswordApiIntegrationTest extends IT {

    @Test
    public void hashesAndVerifiesPassword() {
        Map hashedPassword = authenticatedWebTarget().path("/passwords/hash").request().put(Entity.json(ImmutableMap.of("rawPassword", "my-secret-password"))).readEntity(Map.class);
        Map verifyResponse = authenticatedWebTarget().path("/passwords/verify").request().put(Entity.json(ImmutableMap.of("passwordCandidate", "my-secret-password", "hash", hashedPassword.get("hash")))).readEntity(Map.class);
        assertEquals(true, verifyResponse.get("valid"));
    }
}
