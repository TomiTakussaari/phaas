package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class RootEndpointIntegrationTest extends IT {

    @Test
    public void showsWelcomePageForAllUsers() {
        String welcomePage = unAuthenticatedWebTarget().path("/").request().get(String.class);
        assertThat(welcomePage.contains("Congratulations, you have reached PHAAS!")).isTrue();
    }

    @Test
    public void returnsResponseSignatureForAuthenticatedUserWithSigningKey() {
        Response response = authenticatedWebTarget().path("/users/me").request().get();
        response.close();
        assertThat(response.getHeaderString(SecurityConfig.HmacCalculationAdvice.X_RESPONSE_SIGN)).isNotNull();
    }

    @Test
    public void queryParametersHaveNoEffect() {
        Response response = authenticatedWebTarget().path("/").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        response.close();
    }

    @Test
    public void doesNotReturnsResponseSignatureForUnAuthenticatedUser() {
        Response response = unAuthenticatedWebTarget().path("/").request().get();
        response.close();
        assertThat(response.getHeaderString(SecurityConfig.HmacCalculationAdvice.X_RESPONSE_SIGN)).isNull();
    }

    @Test
    public void showsJsonPageNotFoundPageForAuthenticatedUsers() {
        ExceptionAdvisor.ErrorMessage notFound = authenticatedWebTarget().path("/not-found").request().get().readEntity(ExceptionAdvisor.ErrorMessage.class);
        assertThat(notFound.getStatus()).isEqualTo(404);
    }

    @Test
    public void returnsGeneratedXRequestIdIfNoneGiven() {
        Response response = authenticatedWebTarget().path("/").request().get();
        response.close();
        assertThat(response.getHeaderString(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID)).isNotNull();
    }

    @Test
    public void returnsGivenXRequestIdWhenGiven() {
        Response response = authenticatedWebTarget().path("/").request().header(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID, "r1234").get();
        assertThat(response.getHeaderString(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID)).isEqualTo("r1234");
    }


}
