package com.github.tomitakussaari.phaas.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoHelperTest {

    private final CryptoHelper cryptoHelper = new CryptoHelper(new PepperSource(""));

    @Test
    public void understandsVer1Hash() {
        String version1Data = "$1.d56ad8bfeba742ce.ea2a1e4eaed4021cee946864399ce402308f7d2fdb696c30305f5c117945ce34";
        assertThat(cryptoHelper.decryptData("password", version1Data)).isEqualTo("my-secret-data");
    }

    @Test
    public void understandsVer2Hash() {
        String version2Data = "$2.57b384772a17e677.2b94f67aa84311ad8bdc00deb666e3237b15ce6562e2ff1a94141241d20fac3c1c78d34f4ce3e5545990af5e19c4";
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