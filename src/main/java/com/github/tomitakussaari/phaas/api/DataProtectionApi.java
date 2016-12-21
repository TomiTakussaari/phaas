package com.github.tomitakussaari.phaas.api;


import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.EncryptedAndSignedMessage;
import com.github.tomitakussaari.phaas.model.EncryptedMessage;
import com.github.tomitakussaari.phaas.model.HmacVerificationRequest;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@RestController
@RequestMapping("/data-protection")
public class DataProtectionApi {

    public static final String HASH_VALUE_SEPARATOR = ":::";

    @ApiOperation(value = "Calculates HMAC for given message", consumes = "text/plain")
    @RequestMapping(method = RequestMethod.PUT, path = "/hmac")
    public String calculateHmac(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String message) {
        DataProtectionScheme dataProtectionScheme = userDetails.activeProtectionScheme();
        return dataProtectionScheme.getId() + HASH_VALUE_SEPARATOR + HmacUtils.hmacSha512Hex(userDetails.dataProtectionSchemeForId(dataProtectionScheme.getId()).dataProtectionKey(), message);
    }

    @ApiOperation(value = "Verifies that given HMAC is valid for message")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Hmac was valid for given message"),
            @ApiResponse(code = 422, message = "Hmac was not valid for given message")
    })
    @RequestMapping(method = RequestMethod.PUT, path = "/hmac/valid")
    public ResponseEntity<Void> verifyHmac(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody HmacVerificationRequest request) {
        if (isValidMacFor(request.getMessage(), request.hmac(), userDetails, request.schemeId())) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    @ApiOperation(value = "Encrypts given data using currently active encryption key", consumes = "text/plain")
    @RequestMapping(method = RequestMethod.PUT, path = "/encrypted")
    public String encrypt(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String messageToEncode) {
        return encryptedMessage(userDetails, messageToEncode).getProtectedMessage();
    }

    @ApiOperation(value = "Decrypts given data using currently active encryption key", consumes = "text/plain")
    @RequestMapping(method = RequestMethod.PUT, path = "/decrypted")
    public String decrypt(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String messageToDecode) {
        return decryptedMessage(userDetails, new EncryptedMessage(messageToDecode));
    }

    @ApiOperation(value = "Encrypts and signs given data", consumes = "text/plain")
    @RequestMapping(method = RequestMethod.PUT, path = "/encrypted-and-signed")
    public EncryptedAndSignedMessage encryptAndSign(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody String messageToProtect) {
        EncryptedMessage encryptedMessage = encryptedMessage(userDetails, messageToProtect);
        String hmac = calculateHmac(userDetails, messageToProtect);
        return new EncryptedAndSignedMessage(encryptedMessage.getProtectedMessage(), hmac);
    }

    @ApiOperation(value = "Decrypts and verifies given data")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns decrypted and verified message"),
            @ApiResponse(code = 422, message = "Hmac was not valid for given message")
    })
    @RequestMapping(method = RequestMethod.PUT, path = "/decrypted-and-verified", produces = "text/plain", consumes = "application/json")
    public ResponseEntity decryptAndVerify(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails, @RequestBody EncryptedAndSignedMessage encryptedAndSignedMessage) {
        String decryptedMessage = decryptedMessage(userDetails, encryptedAndSignedMessage.encryptedMessage());
        if(isValidMacFor(decryptedMessage, encryptedAndSignedMessage.verificationRequest().hmac(), userDetails, encryptedAndSignedMessage.encryptedMessage().schemeId())) {
            return ResponseEntity.ok(decryptedMessage);
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    private EncryptedMessage encryptedMessage(PhaasUserDetails userDetails, String messageToEncode) {
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.delux(userDetails.currentDataProtectionScheme().dataProtectionKey(), salt);
        return new EncryptedMessage(userDetails.activeProtectionScheme().getId(), salt, encryptor.encrypt(messageToEncode));
    }

    private String decryptedMessage(PhaasUserDetails userDetails, EncryptedMessage encryptedMessage) {
        TextEncryptor encryptor = Encryptors.delux(userDetails.dataProtectionSchemeForId(encryptedMessage.schemeId()).dataProtectionKey(), encryptedMessage.getSalt());
        return encryptor.decrypt(encryptedMessage.getEncryptedMessage());
    }

    private boolean isValidMacFor(String message, String hmacCandidate, PhaasUserDetails userDetails, int schemeId) {
        String realHmac = HmacUtils.hmacSha512Hex(userDetails.dataProtectionSchemeForId(schemeId).dataProtectionKey(), message);
        return equalsNoEarlyReturn(realHmac, hmacCandidate);
    }

    /**
     * From BCrypt.java
     */
    private static boolean equalsNoEarlyReturn(String a, String b) {
        char[] caa = a.toCharArray();
        char[] cab = b.toCharArray();
        if (caa.length != cab.length) {
            return false;
        } else {
            byte ret = 0;

            for (int i = 0; i < caa.length; ++i) {
                ret = (byte) (ret | caa[i] ^ cab[i]);
            }

            return ret == 0;
        }
    }
}
