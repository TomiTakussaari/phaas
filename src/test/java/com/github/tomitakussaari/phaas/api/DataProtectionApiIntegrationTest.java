package com.github.tomitakussaari.phaas.api;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.client.Entity.text;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DataProtectionApiIntegrationTest extends IT {

    @Test
    public void calculatesAndVerifiesHmacForData() {
        String hmac = authenticatedWebTarget().path("/data-protection/hmac").request().put(text("data-to-protect"), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/hmac/verify").request().put(json(of("message", "data-to-protect", "hmacCandidate", hmac)));
        assertEquals(204, response.getStatus());
    }

    @Test
    public void noticesMangledHmac() {
        String hmac = authenticatedWebTarget().path("/data-protection/hmac").request().put(text("data-to-protect"), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/hmac/verify").request().put(json(of("message", "data-to-protect", "hmacCandidate", hmac+"1")));
        assertEquals(422, response.getStatus());
    }

    @Test
    public void encryptsAndDecryptsData() {
        String protectedData = authenticatedWebTarget().path("/data-protection/encrypt").request().put(text("data-to-protect"), String.class);
        assertNotEquals("data-to-protect", protectedData);
        String decryptedData = authenticatedWebTarget().path("/data-protection/decrypt").request().put(text(protectedData), String.class);
        assertEquals("data-to-protect", decryptedData);
    }
}