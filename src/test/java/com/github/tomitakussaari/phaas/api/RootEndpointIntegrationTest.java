package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

public class RootEndpointIntegrationTest extends IT {

    @Test
    public void showsWelcomePageForAllUsers() {
        String welcomePage = unAuthenticatedWebTarget().path("/").request().get(String.class);
        assertTrue(welcomePage.contains("Congratulations, you have reached PHAAS!"));
    }

    @Test
    public void returnsResponseSignatureForAuthenticatedUserWithSigningKey() {
        Response response = authenticatedWebTarget().path("/users/me").request().get();
        response.close();
        assertNotNull(response.getHeaderString(SecurityConfig.HmacCalculationAdvice.X_RESPONSE_SIGN));
    }

    @Test
    public void doesNotReturnsResponseSignatureForUnAuthenticatedUser() {
        Response response = unAuthenticatedWebTarget().path("/").request().get();
        response.close();
        assertNull(response.getHeaderString(SecurityConfig.HmacCalculationAdvice.X_RESPONSE_SIGN));
    }

    @Test
    public void showsJsonPageNotFoundPageForAuthenticatedUsers() {
        ExceptionAdvisor.ErrorMessage notFound = authenticatedWebTarget().path("/not-found").request().get().readEntity(ExceptionAdvisor.ErrorMessage.class);
        assertEquals(404, notFound.getStatus());
    }

    @Test
    public void returnsGeneratedXRequestIdIfNoneGiven() {
        Response response = authenticatedWebTarget().path("/").request().get();
        response.close();
        assertNotNull(response.getHeaderString(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID));
    }

    @Test
    public void returnsGivenXRequestIdWhenGiven() {
        Response response = authenticatedWebTarget().path("/").request().header(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID, "r1234").get();
        assertEquals("r1234", response.getHeaderString(SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID));
    }


}