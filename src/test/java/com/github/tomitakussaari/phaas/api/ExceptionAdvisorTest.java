package com.github.tomitakussaari.phaas.api;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ExceptionAdvisorTest {

    @Test
    public void mapsExceptionToHttp500() {
        ResponseEntity<ExceptionAdvisor.ErrorMessage> error = new ExceptionAdvisor().genericError(new Exception("error"));
        assertEquals(500, error.getStatusCode().value());
        assertEquals("Internal error", error.getBody().getMessage());
        assertEquals("error", error.getBody().getReason());
    }

    @Test
    public void mapsOperationIsNotAvailableExceptionToHttp500() {
        ResponseEntity<ExceptionAdvisor.ErrorMessage> error = new ExceptionAdvisor().operationNotAvailable(new OperationIsNotAvailableException("not available"));
        assertEquals(405, error.getStatusCode().value());
        assertEquals("Disabled", error.getBody().getMessage());
        assertEquals("not available", error.getBody().getReason());
    }

}