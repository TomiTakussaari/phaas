package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.model.HashedPassword;
import com.github.tomitakussaari.phaas.model.PasswordHashRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyResult;
import com.github.tomitakussaari.phaas.user.PhaasUser;
import com.github.tomitakussaari.phaas.user.UsersService;
import com.github.tomitakussaari.phaas.util.PasswordHasher;
import com.github.tomitakussaari.phaas.util.PasswordVerifier;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/passwords")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PasswordApi {

    private final PasswordVerifier passwordVerifier;
    private final PasswordHasher passwordHasher;

    @ApiOperation(value = "Hashes and protects password")
    @Secured({UsersService.USER_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.PUT, path = "/hash", produces = "application/json")
    public HashedPassword hashPassword(@RequestBody PasswordHashRequest request, @ApiIgnore @AuthenticationPrincipal PhaasUser userDetails) {
        return passwordHasher.hash(request, userDetails.currentlyActiveCryptoData());
    }

    @ApiOperation(value = "Verifies given password against given hash")
    @Secured({UsersService.USER_ROLE_VALUE})
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password was valid"),
            @ApiResponse(code = 422, message = "Password was not valid")
    })
    @RequestMapping(method = RequestMethod.PUT, path = "/verify", produces = "application/json")
    public ResponseEntity<PasswordVerifyResult> verifyPassword(@RequestBody PasswordVerifyRequest request, @ApiIgnore @AuthenticationPrincipal PhaasUser userDetails) {
        PasswordVerifyResult result = passwordVerifier.verify(request, userDetails.cryptoDataForId(request.schemeId()), userDetails.currentlyActiveCryptoData());
        return result.isValid() ? ResponseEntity.ok(result) : ResponseEntity.unprocessableEntity().body(result);
    }


}
