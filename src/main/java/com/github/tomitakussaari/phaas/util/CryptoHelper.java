package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.util.function.BiFunction;

import static com.github.tomitakussaari.phaas.model.DataProtectionScheme.TOKEN_VALUE_SEPARATOR;

public class CryptoHelper {

    enum Version {
        $1(Encryptors::text),
        $2(Encryptors::delux);

        private final BiFunction<CharSequence, String, TextEncryptor> encryptorFunction;

        Version(BiFunction<CharSequence, String, TextEncryptor> encryptorFunction) {
            this.encryptorFunction = encryptorFunction;
        }

        private TextEncryptor encryptor(CharSequence password, String salt) {
            return encryptorFunction.apply(password, salt);
        }

        static Version defaultVersion() {
            return Version.$2;
        }
    }

    public String encryptData(CharSequence password, String dataToEncrypt) {
        Version encryptVersion = Version.defaultVersion();
        String salt = KeyGenerators.string().generateKey();
        return encryptVersion.name() + TOKEN_VALUE_SEPARATOR + salt + TOKEN_VALUE_SEPARATOR + encryptVersion.encryptor(password, salt).encrypt(dataToEncrypt);
    }

    public String decryptData(CharSequence password, String encryptedData) {
        Version version = Version.valueOf(encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[0]);
        String salt = encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[1];
        String data = encryptedData.split(DataProtectionScheme.ESCAPED_TOKEN_VALUE_SEPARATOR)[2];
        return version.encryptor(password, salt).decrypt(data);
    }
}
