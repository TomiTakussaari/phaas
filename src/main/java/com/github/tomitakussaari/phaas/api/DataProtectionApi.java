package com.github.tomitakussaari.phaas.api;


import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static com.github.tomitakussaari.phaas.util.StringHelper.equalsNoEarlyReturn;

@RestController
@RequestMapping("/data-protection")
public class DataProtectionApi {

    @ApiOperation(value = "Calculates HMAC for given message")
    @RequestMapping(method = RequestMethod.PUT, path = "/hmac", produces = "text/plain")
    public String calculateHmac(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String message) {
        return HmacUtils.hmacSha512Hex(userDetails.currentDataEncryptionKey(), message);
    }

    @ApiOperation(value = "Verifies that given HMAC is valid for message")
    @RequestMapping(method = RequestMethod.PUT, path = "/hmac/verify")
    public ResponseEntity<Void> verifyHmac(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody HmacVerificationRequest request) {
        String realHmac = HmacUtils.hmacSha512Hex(userDetails.currentDataEncryptionKey(), request.getMessage());
        if(equalsNoEarlyReturn(realHmac, request.getHmacCandidate())) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    @ApiOperation(value = "Encrypts given data using currently active encryption key")
    @RequestMapping(method = RequestMethod.PUT, path = "/encrypt", produces = "text/plain")
    public String encrypt(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String messageToEncode) {
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.delux(userDetails.currentDataEncryptionKey(), salt);
        return salt + ":::" + encryptor.encrypt(messageToEncode);
    }

    @ApiOperation(value = "Decrypts given data using currently active encryption key")
    @RequestMapping(method = RequestMethod.PUT, path = "/decrypt", produces = "text/plain")
    public String decrypt(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String messageToDecode) {
        String salt = messageToDecode.split(":::")[0];
        String encodedMessage = messageToDecode.split(":::")[1];
        TextEncryptor encryptor = Encryptors.delux(userDetails.currentDataEncryptionKey(), salt);
        return encryptor.decrypt(encodedMessage);
    }

    @Data
    static class HmacVerificationRequest {
        private final String message;
        private final String hmacCandidate;

    }



}
