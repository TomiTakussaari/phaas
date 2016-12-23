package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.model.ProtectionSchemeNotFoundException;
import com.github.tomitakussaari.phaas.user.SecurityConfig;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.xml.bind.annotation.XmlRootElement;

@ControllerAdvice
@Slf4j
public class ExceptionAdvisor {

    @XmlRootElement
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class ErrorMessage {
        private String message;
        private String reason;
        private String requestId;
        private int status;
    }

    @ExceptionHandler(OperationIsNotAvailableException.class)
    public ResponseEntity<ErrorMessage> operationNotAvailable(OperationIsNotAvailableException e) {
        log.info("Tried to do operation that is not available", e);
        return responseEntity("Disabled", e, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> genericError(Exception e) {
        log.warn("Generic error: " + e.getMessage(), e);
        return responseEntity("Internal error", e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> accessDenied(AccessDeniedException e) {
        log.warn("Access was denied: " + e.getMessage(), e);
        return responseEntity("No access", e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ProtectionSchemeNotFoundException.class)
    public ResponseEntity<ErrorMessage> accessDenied(ProtectionSchemeNotFoundException e) {
        log.warn("DataProtectionScheme was not found: " + e.getMessage(), e);
        return responseEntity("DataProtectionScheme was not found", e, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorMessage> responseEntity(String message, Exception e, HttpStatus status) {
        return new ResponseEntity<>(new ErrorMessage(message, e.getMessage(), MDC.get(SecurityConfig.AuditAndLoggingFilter.MDC_REQUEST_ID), status.value()), status);
    }

}