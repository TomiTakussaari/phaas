package com.github.tomitakussaari.phaas.api;

import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.client.Entity.text;
import static org.junit.Assert.*;

public class DataProtectionApiIntegrationTest extends IT {

    @Test
    public void calculatesAndVerifiesHmacForData() {
        String hmac = authenticatedWebTarget().path("/data-protection/hmac").request().put(text("data-to-protect"), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/hmac/valid").request().put(json(of("message", "data-to-protect", "authenticationCode", hmac)));
        assertEquals(204, response.getStatus());
    }

    @Test
    public void noticesMangledHmac() {
        String hmac = authenticatedWebTarget().path("/data-protection/hmac").request().put(text("data-to-protect"), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/hmac/valid").request().put(json(of("message", "data-to-protect", "authenticationCode", hmac + "1")));
        assertEquals(422, response.getStatus());
    }

    @Test
    public void encryptsAndDecryptsData() {
        String protectedData = authenticatedWebTarget().path("/data-protection/encrypted").request().put(text("data-to-protect"), String.class);
        assertNotEquals("data-to-protect", protectedData);
        String decryptedData = authenticatedWebTarget().path("/data-protection/decrypted").request().put(text(protectedData), String.class);
        assertEquals("data-to-protect", decryptedData);
    }

    @Test
    public void decryptsAndVerifiesEncryptedAndSignedData() {
        Map protectedData = authenticatedWebTarget().path("/data-protection/encrypted-and-signed").request().put(text("my-protected-data"), Map.class);
        assertTrue(protectedData.containsKey("protectedMessage"));
        assertTrue(protectedData.containsKey("authenticationCode"));

        String decryptedData = authenticatedWebTarget().path("/data-protection/decrypted-and-verified").request().put(json(protectedData), String.class);
        assertEquals("my-protected-data", decryptedData);
    }

    @Test
    public void noticesChangesInEncryptedAndSignedData() {
        Map protectedData = authenticatedWebTarget().path("/data-protection/encrypted-and-signed").request().put(text("my-protected-data"), Map.class);
        protectedData.put("authenticationCode", protectedData.get("authenticationCode") + "1");
        Response response = authenticatedWebTarget().path("/data-protection/decrypted-and-verified").request().put(json(protectedData));
        assertEquals(422, response.getStatus());
    }
}