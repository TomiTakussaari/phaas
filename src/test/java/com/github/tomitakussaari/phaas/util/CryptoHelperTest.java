package com.github.tomitakussaari.phaas.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoHelperTest {

    private final CryptoHelper cryptoHelper = new CryptoHelper();

    @Test
    public void understandsVer1Hash() {
        String version1Data = "$1.e1617d04eedbe7e1.6174ec0c7b7163be4b3fa59ef976af58378046949788877c9fcd0fc6b19fd44d";
        assertThat(cryptoHelper.decryptData("password", version1Data)).isEqualTo("my-secret-data");
    }

    @Test
    public void understandsVer2Hash() {
        String version2Data = "$2.b441005591ff0cd2.c0956b9e5e8a4001cd63bc97197d5e94abb8863534d919722d5e5a0581b647f9d788903f5156c11d278272db7278";
        assertThat(cryptoHelper.decryptData("password", version2Data)).isEqualTo("my-secret-data");
    }

    @Test
    public void encryptsAndDecryptsData() {
        String encryptedData = cryptoHelper.encryptData("password", "my-secret-data");
        String secretData = cryptoHelper.decryptData("password", encryptedData);
        assertThat(secretData).isEqualTo("my-secret-data");
    }

    @Test(expected = IllegalStateException.class)
    public void cannotDecryptWithWrongPassword() {
        String encryptedData = cryptoHelper.encryptData("password", "my-secret-data");
        cryptoHelper.decryptData("wrong-password", encryptedData);
    }

    @Test(expected = IllegalStateException.class)
    public void noticesDifferencesBetweenVeryLongPasswords() {
        String password = RandomStringUtils.random(200);
        String encryptedData = cryptoHelper.encryptData(password, "my-secret-data");
        String secretData = cryptoHelper.decryptData(password+"1", encryptedData);
        assertThat(secretData).isEqualTo("my-secret-data");
    }

}