package com.github.tomitakussaari.phaas.util;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.user.PhaasUser;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.LocalDateTime.now;

public class JwtHelper {

    private static SignedJWT decryptAndVerifySignature(JWEObject jweObject, CryptoData cryptoData) throws JOSEException {
        jweObject.decrypt(new DirectDecrypter(cryptoData.dataProtectionKey().getBytes()));
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        checkArgument(signedJWT.verify(new MACVerifier(cryptoData.dataProtectionKey().getBytes())), "Signature was not valid");
        return signedJWT;
    }

    private static CryptoData findCryptoData(PhaasUser userDetails, JWEObject jweObject) {
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
                        .compressionAlgorithm(CompressionAlgorithm.DEF)
                        .contentType("Tokens")
                        .build(),
                new Payload(signedJWT));
        jweObject.encrypt(new DirectEncrypter(cryptoData.dataProtectionKey().getBytes()));
        return jweObject;
    }

    public CreatedToken generate(PhaasUser userDetails, Map<String, Object> claims, Optional<Duration> validityTime) {
        try {
            CryptoData cryptoData = userDetails.currentlyActiveCryptoData();

            Builder claimSetBuilder = new Builder().jwtID(UUID.randomUUID().toString()).issueTime(new Date()).issuer(userDetails.getUsername());
            validityTime.ifPresent(ttl -> claimSetBuilder.expirationTime(Date.from(now().plus(ttl).atZone(ZoneId.systemDefault()).toInstant())));
            claims.forEach(claimSetBuilder::claim);
            SignedJWT jwt = signedToken(cryptoData, claimSetBuilder);
            JWEObject jwe = encrypt(cryptoData, jwt);
            return new CreatedToken(jwt.getJWTClaimsSet().getJWTID(), jwe.serialize());
        } catch (JOSEException|ParseException e) {
            throw new JWTException(e);
        }
    }

    public ParsedToken verifyAndGetClaims(PhaasUser userDetails, String token, Map<String, Object> requiredClaims, Optional<Duration> maximumAge) {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            CryptoData cryptoData = findCryptoData(userDetails, jweObject);
            SignedJWT signedJWT = decryptAndVerifySignature(jweObject, cryptoData);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Map<String, Object> actualClaims = new HashMap<>(claimsSet.getClaims());
            ZonedDateTime issuedAt = ZonedDateTime.ofInstant(claimsSet.getIssueTime().toInstant(), ZoneId.systemDefault());

            verifyClaims(requiredClaims, actualClaims);
            verifyIssuer(userDetails.getUsername(), claimsSet.getIssuer());
            verifyAge(issuedAt, maximumAge, Optional.ofNullable(claimsSet.getExpirationTime()));

            return new ParsedToken(removeInternallyUsedClaims(actualClaims), issuedAt, claimsSet.getJWTID());
        } catch (JOSEException | ParseException | IllegalArgumentException e) {
            throw new JWTException(e);
        }
    }

    private void verifyAge(ZonedDateTime issuedAt, Optional<Duration> maximumAge, Optional<Date> expirationTimeMaybe) {
        ZonedDateTime now = ZonedDateTime.now();
        maximumAge.ifPresent(maxAge -> checkArgument(issuedAt.plus(maxAge).isAfter(now), "Token is too old, max allowed="+maxAge+ " actual="+Duration.between(issuedAt, now)));

        expirationTimeMaybe.ifPresent(expirationTime -> checkArgument(expirationTime.after(new Date()), "Token has expired at "+expirationTime));
    }

    private void verifyIssuer(String expectedIssuer, String actualIssuer) {
        checkArgument(expectedIssuer.equals(actualIssuer), "Issued by wrong party: "+actualIssuer+ " when expected: "+expectedIssuer);
    }

    private Map<String, Object> removeInternallyUsedClaims(Map<String, Object> actualClaims) {
        actualClaims.remove("iat");
        actualClaims.remove("iss");
        actualClaims.remove("jti");
        return actualClaims;
    }

    @RequiredArgsConstructor
    @Getter
    public static class CreatedToken {
        @NonNull
        private final String id;
        @NonNull
        private final String token;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ParsedToken {
        @NonNull
        private final Map<String, Object> claims;
        @NonNull
        private final ZonedDateTime issuedAt;
        @NonNull
        private final String id;
    }

    public static class JWTException extends RuntimeException {
        JWTException(Exception e) {
            super(e.getMessage(), e);
        }
    }
}
