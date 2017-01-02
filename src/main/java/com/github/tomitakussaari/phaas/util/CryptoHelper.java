package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.TOKEN_VALUE_SEPARATOR;

public class CryptoHelper {

    public String encryptData(CharSequence password, String dataToEncrypt) {
        String salt = KeyGenerators.string().generateKey();
        return salt + TOKEN_VALUE_SEPARATOR + encryptor(password, salt).encrypt(dataToEncrypt);
    }

    public String decryptData(CharSequence password, String encryptedData) {
        String salt = encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[0];
        String data = encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
        return encryptor(password, salt).decrypt(data);
    }

    public static TextEncryptor encryptor(CharSequence password, String salt) {
        return Encryptors.text(password, salt);
    }
}
