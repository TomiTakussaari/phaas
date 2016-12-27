package com.github.tomitakussaari.phaas.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.jsonwebtoken.CompressionCodecs;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.client.Entity.text;
import static org.junit.Assert.*;

public class DataProtectionApiIntegrationTest extends IT {

    @Test
    public void createsAndVerifiesJsonWebToken() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), String.class);
        Map response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token)), Map.class);
        assertEquals("value1", response.get("claim1"));
        assertEquals("value2", response.get("claim2"));
    }

    @Test
    public void jsonWebTokenCanBeValidatedAfterPasswordChange() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), String.class);

        Response passwordChangeResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("password", "new-password")));
        assertEquals(200, passwordChangeResponse.getStatus());

        Map response = unAuthenticatedWebTarget().path("/data-protection/token").request()
                .header("Authorization", basicAuth(USER_NAME, "new-password"))
                .put(json(ImmutableMap.of("token", token)), Map.class);
        assertEquals("value1", response.get("claim1"));
        assertEquals("value2", response.get("claim2"));
    }

    @Test
    public void noticesMangledDataInJWTToken() throws IOException {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), String.class);
        String dataB64 = token.split("\\.")[1];
        String data = new String(CompressionCodecs.DEFLATE.decompress(Base64.getUrlDecoder().decode(dataB64)));
        Map dataMap = new ObjectMapper().readValue(data, Map.class);
        dataMap.put("claim1", "new-value");
        byte[] bytes = new ObjectMapper().writeValueAsString(dataMap).getBytes();
        String newData = new String(Base64.getUrlEncoder().encode(CompressionCodecs.DEFLATE.compress(bytes)));
        String newToken = token.split("\\.")[0] + "." + newData + "." + token.split("\\.")[2];
        Response response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", newToken, "requiredClaims", ImmutableMap.of())));
        assertEquals(422, response.getStatus());
        assertEquals("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.", response.readEntity(Map.class).get("message"));

    }

    @Test
    public void generatesIssuedAtClaimToJsonWebToken() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("a", "b"))), String.class);
        Map response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token)), Map.class);
        assertNotNull(Integer.valueOf(response.get("iat").toString()));
    }

    @Test
    public void createsAndVerifiesJsonWebTokenWithWantedClaims() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), String.class);
        authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token, "requiredClaims", ImmutableMap.of("my-claim", "is-true"))), Map.class);
    }

    @Test
    public void jwtWithPreviousProtectionSchemeIsStillValid() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), String.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT")));
        assertEquals(Response.Status.Family.SUCCESSFUL, updateResponse.getStatusInfo().getFamily());
        authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token, "requiredClaims", ImmutableMap.of("my-claim", "is-true"))), Map.class);
    }

    @Test
    public void failsWhenJWTDoesNotContainWantedClaim() {
        String token = authenticatedWebTarget().path("/data-protection/token").request().post(json(ImmutableMap.of("algorithm", "HS256", "claims", ImmutableMap.of("claim1", "value1"))), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/token").request().put(json(ImmutableMap.of("token", token, "requiredClaims", ImmutableMap.of("my-claim", "is-true"))));
        assertEquals(422, response.getStatus());
        assertEquals("Expected my-claim claim to be: is-true, but was not present in the JWT claims.", response.readEntity(Map.class).get("message"));
    }

    @Test
    public void calculatesAndVerifiesHmacForData() {
        String hmac = authenticatedWebTarget().path("/data-protection/hmac").request().post(text("data-to-protect"), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/hmac").request().put(json(of("message", "data-to-protect", "authenticationCode", hmac)));
        assertEquals(204, response.getStatus());
    }

    @Test
    public void noticesMangledHmac() {
        String hmac = authenticatedWebTarget().path("/data-protection/hmac").request().post(text("data-to-protect"), String.class);
        Response response = authenticatedWebTarget().path("/data-protection/hmac").request().put(json(of("message", "data-to-protect", "authenticationCode", hmac + "1")));
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