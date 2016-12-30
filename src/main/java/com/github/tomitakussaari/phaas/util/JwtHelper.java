package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@UtilityClass
public class JwtHelper {

    public static String generate(PhaasUserDetails userDetails, Map<String, Object> claims) {
        try {
            CryptoData cryptoData = userDetails.currentlyActiveCryptoData();
            JWSSigner signer = new MACSigner(cryptoData.dataProtectionKey().getBytes());

            JWTClaimsSet.Builder claimSetBuilder = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())

                    .issueTime(new Date());

            claims.forEach(claimSetBuilder::claim);

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimSetBuilder.build());

            signedJWT.sign(signer);

            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                            .keyID(String.valueOf(cryptoData.getScheme().getId()))
                            .contentType("JWT")
                            .build(),
                    new Payload(signedJWT));

            jweObject.encrypt(new DirectEncrypter(cryptoData.dataProtectionKey().getBytes()));
            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new JWTException(e);
        }
    }

    public static Map<String, Object> verifyAndGetClaims(PhaasUserDetails userDetails, String token, Map<String, Object> requiredClaims) {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            Integer schemeId = Integer.valueOf(jweObject.getHeader().getKeyID());
            CryptoData cryptoData = userDetails.cryptoDataForId(schemeId);

            jweObject.decrypt(new DirectDecrypter(cryptoData.dataProtectionKey().getBytes()));

            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();

            checkArgument(signedJWT.verify(new MACVerifier(cryptoData.dataProtectionKey().getBytes())), "Signature was not valid");

            Map<String, Object> actualClaims = signedJWT.getJWTClaimsSet().getClaims();

            return verifyClaims(requiredClaims, actualClaims);
        } catch (JOSEException | ParseException | IllegalArgumentException e) {
            throw new JWTException(e);
        }

    }

    private static Map<String, Object> verifyClaims(Map<String, Object> requiredClaims, Map<String, Object> actualClaims) {
        requiredClaims.forEach((claim, expectedValue) ->
        {
            String errorMessage = "Wrong value for " + claim + " required: " + expectedValue + " but was " + actualClaims.get(claim);
            checkArgument(expectedValue.equals(actualClaims.get(claim)), errorMessage);
        });
        return actualClaims;
    }

    public static class JWTException extends RuntimeException {
        JWTException(Exception e) {
            super(e.getMessage(), e);
        }
    }
}
