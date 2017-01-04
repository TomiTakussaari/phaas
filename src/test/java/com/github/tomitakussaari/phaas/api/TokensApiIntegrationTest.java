package com.github.tomitakussaari.phaas.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;

public class TokensApiIntegrationTest extends IT {

    @Test
    public void createsAndVerifiesJsonWebToken() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), Map.class);
        Map response = authenticatedWebTarget().path("/tokens").request().put(json(ImmutableMap.of("token", token.get("token"))), Map.class);
        Map claims = (Map) response.get("claims");
        assertThat(claims.get("claim1")).isEqualTo("value1");
        assertThat(claims.get("claim2")).isEqualTo("value2");
    }

    @Test
    public void jsonWebTokenCanBeValidatedAfterPasswordChange() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("claim1", "value1", "claim2", "value2"))), Map.class);

        Response passwordChangeResponse = authenticatedWebTarget().path("/users/me").request().put(Entity.json(of("password", "new-password")));
        assertThat(passwordChangeResponse.getStatus()).isEqualTo(200);

        Map response = unAuthenticatedWebTarget().path("/tokens").request()
                .header("Authorization", basicAuth(USER_NAME, "new-password"))
                .put(json(ImmutableMap.of("token", token.get("token"))), Map.class);
        Map claims = (Map) response.get("claims");
        assertThat(claims.get("claim1")).isEqualTo("value1");
        assertThat(claims.get("claim2")).isEqualTo("value2");
    }

    @Test
    public void returnsIssuedAtInfoInParsedToken() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("a", "b"))), Map.class);
        Map response = authenticatedWebTarget().path("/tokens").request().put(json(ImmutableMap.of("token", token.get("token"))), Map.class);
        Map claims = (Map) response.get("claims");
        assertThat(response.get("issuedAt")).isNotNull();
        assertThat(claims.get("iat")).isNull();
    }

    @Test
    public void createsAndVerifiesJsonWebTokenWithWantedClaims() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), Map.class);
        authenticatedWebTarget().path("/tokens").request().put(json(ImmutableMap.of("token", token.get("token"), "requiredClaims", ImmutableMap.of("my-claim", "is-true"))), Map.class);
    }

    @Test
    public void rejectsTokenThatIsIssuedTooLongTimeAgo() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), Map.class);
        Response response = authenticatedWebTarget().path("/tokens").request().put(json(ImmutableMap.of("token", token.get("token"), "maxAcceptedAge", Duration.ofNanos(1))));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(Map.class).get("message").toString()).contains("Token is too old, max allowed=PT0.000000001S actual=");
    }

    @Test
    public void jwtWithPreviousProtectionSchemeIsStillValid() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("my-claim", "is-true", "claim2", "value2"))), Map.class);
        Response updateResponse = authenticatedWebTarget().path("/users/me/scheme").request().post(Entity.json(of("algorithm", "SHA256_BCRYPT")));
        assertThat(updateResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        authenticatedWebTarget().path("/tokens").request().put(json(ImmutableMap.of("token", token.get("token"), "requiredClaims", ImmutableMap.of("my-claim", "is-true"))), Map.class);
    }

    @Test
    public void failsWhenJWTDoesNotContainWantedClaim() {
        Map token = authenticatedWebTarget().path("/tokens").request().post(json(ImmutableMap.of("claims", ImmutableMap.of("claim1", "value1"))), Map.class);
        Response response = authenticatedWebTarget().path("/tokens").request().put(json(ImmutableMap.of("token", token.get("token"), "requiredClaims", ImmutableMap.of("my-claim", "is-true"))));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(Map.class).get("message")).isEqualTo("Wrong value for my-claim required: is-true but was null");
    }
}
