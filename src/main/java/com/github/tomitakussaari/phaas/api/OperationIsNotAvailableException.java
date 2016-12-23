package com.github.tomitakussaari.phaas.api;

public class OperationIsNotAvailableException extends RuntimeException {
    public OperationIsNotAvailableException(String message) {
        super(message);
    }
}
