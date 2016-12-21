package com.github.tomitakussaari.phaas.model;

import lombok.Getter;

public class EncryptedMessage {
    private static final String HASH_VALUE_SEPARATOR = ":::";

    @Getter
    private final String protectedMessage;
    public
    EncryptedMessage(int schemeId, String salt, String encryptedMessage) {
        this.protectedMessage = schemeId +HASH_VALUE_SEPARATOR+ salt + HASH_VALUE_SEPARATOR + encryptedMessage;
    }

    public EncryptedMessage(String encryptedMessageWithSalt) {
        this.protectedMessage = encryptedMessageWithSalt;
    }

    public String getSalt() {
        return protectedMessage.split(HASH_VALUE_SEPARATOR)[1];
    }

    public String getEncryptedMessage() {
        return protectedMessage.split(HASH_VALUE_SEPARATOR)[2];
    }

    public int schemeId() {
        return Integer.valueOf(protectedMessage.split(HASH_VALUE_SEPARATOR)[0]);
    }

}
