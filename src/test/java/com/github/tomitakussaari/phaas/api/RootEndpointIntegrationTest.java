package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RootEndpointIntegrationTest extends IT {

    @Test
    public void errorPath() {
        assertThat(new RootEndpoint().getErrorPath()).isEqualTo("/error");
    }

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
        Response response = authenticatedWebTarget().path("/").queryParam("foo", "bar").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        response.close();
    }

    @Test
    public void returnsErrorMessageForUnknownPage() {
        Response response = authenticatedWebTarget().path("/does-not-exists").request().get();
        assertThat(response.getStatus()).isEqualTo(404);
        Map errorMessage = response.readEntity(Map.class);
        assertThat(errorMessage).containsEntry("status", 404);
        assertThat(errorMessage).containsEntry("reason", "Not Found");
    }

    @Test
    public void returnsErrorMessageForUnknownPageInXml() {
        Response response = authenticatedWebTarget().path("/does-not-exists").request().accept(MediaType.APPLICATION_XML).get();
        assertThat(response.getStatus()).isEqualTo(404);
        String errorMessage = response.readEntity(String.class);
        assertThat(errorMessage).contains("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><errorMessage>");
        assertThat(errorMessage).contains("<status>404</status>");
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
