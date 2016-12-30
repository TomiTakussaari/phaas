package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import com.github.tomitakussaari.phaas.user.UsersService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class JwtHelperTest {

    private final PhaasUserDetails userDetails = Mockito.mock(PhaasUserDetails.class);
    private final String secretKey = UsersService.randomKey();
    private final CryptoData cryptoData = Mockito.mock(CryptoData.class);
    private final DataProtectionScheme scheme = Mockito.mock(DataProtectionScheme.class);

    @Before
    public void before() {
        when(userDetails.currentlyActiveCryptoData()).thenReturn(cryptoData);
        when(userDetails.getUsername()).thenReturn("my-user");
        when(userDetails.cryptoDataForId(1)).thenReturn(cryptoData);
        when(cryptoData.dataProtectionKey()).thenReturn(secretKey);
        when(cryptoData.getScheme()).thenReturn(scheme);
        when(scheme.getId()).thenReturn(1);
    }

    @Test
    public void generateAndParse() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = JwtHelper.generate(userDetails, claims);
        Map<String, Object> parsedClaims = JwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of());
        assertEquals(claims.get("claim1"), parsedClaims.get("claim1"));
    }

    @Test
    public void setsIssuedAtIssuerAndTokenIdClaimsAutomatically() {
        ImmutableMap<String, Object> claims = ImmutableMap.of();
        String token = JwtHelper.generate(userDetails, claims);
        Map<String, Object> parsedClaims = JwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of());
        assertNotNull(parsedClaims.get("jti"));
        assertNotNull(parsedClaims.get("iat"));
        assertEquals("my-user", parsedClaims.get("iss"));
    }

    @Test
    public void rejectsTokensWithoutRequiredClaim() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = JwtHelper.generate(userDetails, claims);
        try {
            JwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("claim2", "value2"));
        } catch (JwtHelper.JWTException e) {
            assertEquals("Wrong value for claim2 required: value2 but was null", e.getMessage());
        }
    }

    @Test
    public void rejectsTokensWithWrongValueForRequiredClaim() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = JwtHelper.generate(userDetails, claims);
        try {
            JwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("claim1", "something-else"));
            fail("should not have succeeded");
        } catch (JwtHelper.JWTException e) {
            assertEquals("Wrong value for claim1 required: something-else but was value1", e.getMessage());
        }
    }

    @Test
    public void canParseExistingToken() {
        when(cryptoData.dataProtectionKey()).thenReturn("CJ;h*ywnWV4t9_+BN?~]}&P~E8uEAme@");
        String oldToken = "eyJ6aXAiOiJERUYiLCJraWQiOiIxIiwiY3R5IjoiSldUIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..auZ902bJ1qNGS0P-.UO1SFi3qtsknfORNjL5Hp0bq1nzy4TpCOHzI7K-gtoKrTC5xVqkJB-kOzB_lnPnYsYo2x_Z375T5HVRa8N9uxoFUvx0naDDznKwvt3CvgiWqEgA4E0Hyg4EyqMKiazVKeN9zpH-cMtd6VjCcziXfXBi_iMhvUyeZMJANZUg1BfgOU0K1cjR_jc_LrRiMv7Bcy0Y3ygV2NheiNULqBj5Ob6xg8trgoIp_-F5NbQ.vzIhXbpCESR4EjGekJEQgA";
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        JwtHelper.verifyAndGetClaims(userDetails, oldToken, claims);
    }

    @Test
    public void refusesToParseTokenWithInvalidKey() {
        when(cryptoData.dataProtectionKey()).thenReturn("AJ;h*ywnWV4t9_+BN?~]}&P~E8uEAme@");
        String oldToken = "eyJ6aXAiOiJERUYiLCJraWQiOiIxIiwiY3R5IjoiSldUIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..auZ902bJ1qNGS0P-.UO1SFi3qtsknfORNjL5Hp0bq1nzy4TpCOHzI7K-gtoKrTC5xVqkJB-kOzB_lnPnYsYo2x_Z375T5HVRa8N9uxoFUvx0naDDznKwvt3CvgiWqEgA4E0Hyg4EyqMKiazVKeN9zpH-cMtd6VjCcziXfXBi_iMhvUyeZMJANZUg1BfgOU0K1cjR_jc_LrRiMv7Bcy0Y3ygV2NheiNULqBj5Ob6xg8trgoIp_-F5NbQ.vzIhXbpCESR4EjGekJEQgA";
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        try {
            JwtHelper.verifyAndGetClaims(userDetails, oldToken, claims);
            fail("should not have succeeded");
        } catch (JwtHelper.JWTException e) {
            assertEquals("AES/GCM/NoPadding decryption failed: Tag mismatch!", e.getMessage());
        }
    }
}