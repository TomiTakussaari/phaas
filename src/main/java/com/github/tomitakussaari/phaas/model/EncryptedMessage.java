package com.github.tomitakussaari.phaas.model;

import lombok.Getter;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.TOKEN_VALUE_SEPARATOR;
import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR;

public class EncryptedMessage {


    @Getter
    private final String protectedMessage;
    public
    EncryptedMessage(int schemeId, String salt, String encryptedMessage) {
        this.protectedMessage = schemeId + TOKEN_VALUE_SEPARATOR + salt + TOKEN_VALUE_SEPARATOR + encryptedMessage;
    }

    public EncryptedMessage(String encryptedMessageWithSalt) {
        this.protectedMessage = encryptedMessageWithSalt;
    }

    public String getSalt() {
        return protectedMessage.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
    }

    public String getEncryptedMessage() {
        return protectedMessage.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[2];
    }

    public int schemeId() {
        return Integer.valueOf(protectedMessage.split(ESCAPED_TOKEN_VALUE_SEPARATOR)[0]);
    }

}
