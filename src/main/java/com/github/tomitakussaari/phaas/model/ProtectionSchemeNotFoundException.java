package com.github.tomitakussaari.phaas.model;

public class ProtectionSchemeNotFoundException extends IllegalStateException {

    public ProtectionSchemeNotFoundException(String message) {
        super(message);
    }
}
