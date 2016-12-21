package com.github.tomitakussaari.phaas.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EncryptedAndSignedMessage {
    @Getter
    private final String protectedMessage;
    @Getter
    private final String authenticationCode;


    public HmacVerificationRequest verificationRequest() {
        return new HmacVerificationRequest(protectedMessage, authenticationCode);
    }

    public EncryptedMessage encryptedMessage() {
        return new EncryptedMessage(protectedMessage);
    }
}
