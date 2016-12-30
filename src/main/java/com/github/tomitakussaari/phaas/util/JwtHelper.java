package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
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

            Builder claimSetBuilder = new Builder().jwtID(UUID.randomUUID().toString()).issueTime(new Date());

            claims.forEach(claimSetBuilder::claim);

            return encrypt(cryptoData, signedToken(cryptoData, claimSetBuilder)).serialize();
        } catch (JOSEException e) {
            throw new JWTException(e);
        }
    }

    public static Map<String, Object> verifyAndGetClaims(PhaasUserDetails userDetails, String token, Map<String, Object> requiredClaims) {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            CryptoData cryptoData = findCryptoData(userDetails, jweObject);

            Map<String, Object> actualClaims = decryptAndVerifySignature(jweObject, cryptoData).getJWTClaimsSet().getClaims();

            return verifyClaims(requiredClaims, actualClaims);
        } catch (JOSEException | ParseException | IllegalArgumentException e) {
            throw new JWTException(e);
        }
    }

    private static SignedJWT decryptAndVerifySignature(JWEObject jweObject, CryptoData cryptoData) throws JOSEException {
        jweObject.decrypt(new DirectDecrypter(cryptoData.dataProtectionKey().getBytes()));
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        checkArgument(signedJWT.verify(new MACVerifier(cryptoData.dataProtectionKey().getBytes())), "Signature was not valid");
        return signedJWT;
    }

    private static CryptoData findCryptoData(PhaasUserDetails userDetails, JWEObject jweObject) {
        Integer schemeId = Integer.valueOf(jweObject.getHeader().getKeyID());
        return userDetails.cryptoDataForId(schemeId);
    }

    private static Map<String, Object> verifyClaims(Map<String, Object> requiredClaims, Map<String, Object> actualClaims) {
        requiredClaims.forEach((claim, expectedValue) ->
        {
            String errorMessage = "Wrong value for " + claim + " required: " + expectedValue + " but was " + actualClaims.get(claim);
            checkArgument(expectedValue.equals(actualClaims.get(claim)), errorMessage);
        });
        return actualClaims;
    }

    private static SignedJWT signedToken(CryptoData cryptoData, Builder claimSetBuilder) throws JOSEException {
        JWSSigner signer = new MACSigner(cryptoData.dataProtectionKey().getBytes());
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimSetBuilder.build());
        signedJWT.sign(signer);
        return signedJWT;
    }

    private static JWEObject encrypt(CryptoData cryptoData, SignedJWT signedJWT) throws JOSEException {
        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                        .keyID(String.valueOf(cryptoData.getScheme().getId()))
                        .contentType("JWT")
                        .build(),
                new Payload(signedJWT));

        jweObject.encrypt(new DirectEncrypter(cryptoData.dataProtectionKey().getBytes()));
        return jweObject;
    }


    public static class JWTException extends RuntimeException {
        JWTException(Exception e) {
            super(e.getMessage(), e);
        }
    }
}
