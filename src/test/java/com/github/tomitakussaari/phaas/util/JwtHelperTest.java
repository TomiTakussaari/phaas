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
import static org.mockito.Mockito.when;

public class JwtHelperTest {

    private final PhaasUserDetails userDetails = Mockito.mock(PhaasUserDetails.class);
    private final String secretKey = UsersService.randomKey();
    private final CryptoData cryptoData = Mockito.mock(CryptoData.class);
    private final DataProtectionScheme scheme = Mockito.mock(DataProtectionScheme.class);

    @Before
    public void before() {
        when(userDetails.currentlyActiveCryptoData()).thenReturn(cryptoData);
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
    public void rejectsTokensWithoutRequiredClaim() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = JwtHelper.generate(userDetails, claims);
        try {
            JwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("claim2", "value2"));
        } catch(JwtHelper.JWTException e) {
            assertEquals("Wrong value for claim2 required: value2 but was null", e.getMessage());
        }
    }

    @Test
    public void rejectsTokensWithWrongValueForRequiredClaim() {
        ImmutableMap<String, Object> claims = ImmutableMap.of("claim1", "value1");
        String token = JwtHelper.generate(userDetails, claims);
        try {
            JwtHelper.verifyAndGetClaims(userDetails, token, ImmutableMap.of("claim1", "something-else"));
        } catch(JwtHelper.JWTException e) {
            assertEquals("Wrong value for claim1 required: something-else but was value1", e.getMessage());
        }
    }
}