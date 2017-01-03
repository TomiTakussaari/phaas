package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.TOKEN_VALUE_SEPARATOR;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class CryptoHelper {

    private final PepperSource pepperSource;

    public String encryptData(CharSequence password, String dataToEncrypt) {
        Version encryptVersion = Version.defaultVersion();
        String salt = KeyGenerators.string().generateKey();
        String pepperedPassword = password+"."+ pepperSource.getPepper();
        return encryptVersion.name() + TOKEN_VALUE_SEPARATOR + salt + TOKEN_VALUE_SEPARATOR + encryptVersion.encryptor(pepperedPassword, salt).encrypt(dataToEncrypt);
    }

    public String decryptData(CharSequence password, String encryptedData) {
        Version version = Version.valueOf(encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[0]);
        String salt = encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
        String data = encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[2];
        String pepperedPassword = password+"."+ pepperSource.getPepper();
        return version.encryptor(pepperedPassword, salt).decrypt(data);
    }
}

enum Version {
    $1(Encryptors::text),
    $2(Encryptors::delux);

    private final BiFunction<CharSequence, String, TextEncryptor> encryptorFunction;

    Version(BiFunction<CharSequence, String, TextEncryptor> encryptorFunction) {
        this.encryptorFunction = encryptorFunction;
    }

    TextEncryptor encryptor(CharSequence password, String salt) {
        return encryptorFunction.apply(password, salt);
    }

    static Version defaultVersion() {
        return Version.$2;
    }
}

