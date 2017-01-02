package com.github.tomitakussaari.phaas.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoHelperTest {

    private final CryptoHelper cryptoHelper = new CryptoHelper();

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