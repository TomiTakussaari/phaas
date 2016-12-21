package com.github.tomitakussaari.phaas.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RootEndpointIntegrationTest extends IT {

    @Test
    public void showsWelcomePageForAllUsers() {
        String welcomePage = unAuthenticatedWebTarget().path("/").request().get(String.class);
        assertTrue(welcomePage.contains("Congratulations, you have reached PHAAS!"));
    }

    @Test
    public void showsJsonPageNotFoundPageForAuthenticatedUsers() {
        ExceptionAdvisor.ErrorMessage notFound = authenticatedWebTarget().path("/not-found").request().get().readEntity(ExceptionAdvisor.ErrorMessage.class);
        assertEquals(404, notFound.getStatus());
    }

}