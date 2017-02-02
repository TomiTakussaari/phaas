package com.github.tomitakussaari.phaas.api;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionAdvisorTest {

    @Test
    public void noArgsConstructorForErrorMessage() {
        new ExceptionAdvisor.ErrorMessage();
    }

    @Test
    public void settersForErrorMessage() {
        ExceptionAdvisor.ErrorMessage errorMessage = new ExceptionAdvisor.ErrorMessage();
        errorMessage.setMessage("message");
        errorMessage.setReason("reason");
        errorMessage.setRequestId("id");
        errorMessage.setStatus(404);
        assertThat(errorMessage).isEqualToComparingFieldByFieldRecursively(new ExceptionAdvisor.ErrorMessage("message", "reason", "id", 404));
    }

    @Test
    public void mapsExceptionToHttp500() {
        ResponseEntity<ExceptionAdvisor.ErrorMessage> error = new ExceptionAdvisor().genericError(new Exception("error"));
        assertThat(error.getStatusCode().value()).isEqualTo(500);
        assertThat(error.getBody().getMessage()).isEqualTo("Internal error");
        assertThat(error.getBody().getReason()).isEqualTo("error");
    }

    @Test
    public void mapsOperationIsNotAvailableExceptionToHttp500() {
        ResponseEntity<ExceptionAdvisor.ErrorMessage> error = new ExceptionAdvisor().operationNotAvailable(new OperationIsNotAvailableException("not available"));
        assertThat(error.getStatusCode().value()).isEqualTo(405);
        assertThat(error.getBody().getMessage()).isEqualTo("Disabled");
        assertThat(error.getBody().getReason()).isEqualTo("not available");
    }

}
