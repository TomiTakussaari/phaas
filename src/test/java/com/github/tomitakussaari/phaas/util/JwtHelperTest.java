package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.user.PhaasUser;
import com.github.tomitakussaari.phaas.user.UsersService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

public class JwtHelperTest {

    private final PhaasUser userDetails = Mockito.mock(PhaasUser.class);
    private final String secretKey = UsersService.randomKey();
    private final CryptoData cryptoData = Mockito.mock(CryptoData.class);
    private final DataProtectionScheme scheme = Mockito.mock(DataProtectionScheme.class);
    private final JwtHelper jwtHelper = new JwtHelper();

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
        String token = jwtHelper.generate(userDetails, claims);
        Map<String, Object> parsedClaims = jwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of());
        assertThat(parsedClaims.get("claim1")).isEqualTo(claims.get("claim1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void generateAndParseWithMoreComplexClaims() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("nationalId", "010100-123D", "customerids", asList(1232L, 412412L, 1242141L, 213211L, 12412412L, 121231L), "tags", ImmutableMap.of("difficult-customer", true, "is-always-wrong", false), "admin", true);
        String token = jwtHelper.generate(userDetails, claims);
        Map<String, Object> parsedClaims = jwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("admin", true));
        assertThat(parsedClaims.get("nationalId")).isEqualTo("010100-123D");
        assertThat((List) parsedClaims.get("customerids")).containsExactlyInAnyOrder(1232L, 412412L, 1242141L, 213211L, 12412412L, 121231L);
        assertThat((Map) parsedClaims.get("tags")).containsAllEntriesOf(ImmutableMap.of("difficult-customer", true, "is-always-wrong", false));
    }

    @Test
    public void setsIssuedAtIssuerAndTokenIdClaimsAutomatically() {
        ImmutableMap<String, Object> claims = ImmutableMap.of();
        String token = jwtHelper.generate(userDetails, claims);
        Map<String, Object> parsedClaims = jwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of());
        assertThat(parsedClaims.get("jti")).isNotNull();
        assertThat(parsedClaims.get("iat")).isNotNull();
        assertThat(parsedClaims.get("iss")).isEqualTo("my-user");
    }

    @Test
    public void rejectsTokensWithoutRequiredClaim() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = jwtHelper.generate(userDetails, claims);
        try {
            jwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("claim2", "value2"));
        } catch (JwtHelper.JWTException e) {
            assertThat(e.getMessage()).isEqualTo("Wrong value for claim2 required: value2 but was null");
        }
    }

    @Test
    public void rejectsTokensWithWrongValueForRequiredClaim() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = jwtHelper.generate(userDetails, claims);
        try {
            jwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("claim1", "something-else"));
            fail("should not have succeeded");
        } catch (JwtHelper.JWTException e) {
            assertThat(e.getMessage()).isEqualTo("Wrong value for claim1 required: something-else but was value1");
        }
    }

    @Test(expected = JwtHelper.JWTException.class)
    public void refufesTokenThatIsProtectedInUnacceptableWay() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
        jwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of());
    }

    @Test
    public void canParseExistingToken() {
        when(cryptoData.dataProtectionKey()).thenReturn("CJ;h*ywnWV4t9_+BN?~]}&P~E8uEAme@");
        String oldToken = "eyJ6aXAiOiJERUYiLCJraWQiOiIxIiwiY3R5IjoiSldUIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..auZ902bJ1qNGS0P-.UO1SFi3qtsknfORNjL5Hp0bq1nzy4TpCOHzI7K-gtoKrTC5xVqkJB-kOzB_lnPnYsYo2x_Z375T5HVRa8N9uxoFUvx0naDDznKwvt3CvgiWqEgA4E0Hyg4EyqMKiazVKeN9zpH-cMtd6VjCcziXfXBi_iMhvUyeZMJANZUg1BfgOU0K1cjR_jc_LrRiMv7Bcy0Y3ygV2NheiNULqBj5Ob6xg8trgoIp_-F5NbQ.vzIhXbpCESR4EjGekJEQgA";
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        jwtHelper.verifyAndGetClaims(userDetails, oldToken, claims);
    }

    @Test
    public void refusesToParseTokenWithInvalidKey() {
        when(cryptoData.dataProtectionKey()).thenReturn("AJ;h*ywnWV4t9_+BN?~]}&P~E8uEAme@");
        String oldToken = "eyJ6aXAiOiJERUYiLCJraWQiOiIxIiwiY3R5IjoiSldUIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..auZ902bJ1qNGS0P-.UO1SFi3qtsknfORNjL5Hp0bq1nzy4TpCOHzI7K-gtoKrTC5xVqkJB-kOzB_lnPnYsYo2x_Z375T5HVRa8N9uxoFUvx0naDDznKwvt3CvgiWqEgA4E0Hyg4EyqMKiazVKeN9zpH-cMtd6VjCcziXfXBi_iMhvUyeZMJANZUg1BfgOU0K1cjR_jc_LrRiMv7Bcy0Y3ygV2NheiNULqBj5Ob6xg8trgoIp_-F5NbQ.vzIhXbpCESR4EjGekJEQgA";
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        try {
            jwtHelper.verifyAndGetClaims(userDetails, oldToken, claims);
            fail("should not have succeeded");
        } catch (JwtHelper.JWTException e) {
            assertThat(e.getMessage()).isEqualTo("AES/GCM/NoPadding decryption failed: Tag mismatch!");
        }
    }
}