package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.model.HashedPassword;
import com.github.tomitakussaari.phaas.model.PasswordHashRequest;
import com.github.tomitakussaari.phaas.model.PasswordVerifyResult;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import com.github.tomitakussaari.phaas.util.PasswordHasher;
import com.github.tomitakussaari.phaas.user.ApiUsersService;
import com.github.tomitakussaari.phaas.model.PasswordVerifyRequest;
import com.github.tomitakussaari.phaas.util.PasswordVerifier;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @ApiOperation(value = "Protects password and returns hash from it")
    @Secured({ApiUsersService.USER_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.PUT, path = "/hash", produces = "application/json")
    public HashedPassword hashPassword(@RequestBody PasswordHashRequest request, @ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return passwordHasher.hash(request, userDetails.activeProtectionScheme());
    }

    @ApiOperation(value = "Verifies given password against given hash")
    @Secured({ApiUsersService.USER_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.PUT, path = "/verify", produces = "application/json")
    public PasswordVerifyResult verifyPassword(@RequestBody PasswordVerifyRequest request, @ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return passwordVerifier.verify(request, userDetails.protectionScheme(request.schemeId()), userDetails.activeProtectionScheme());
    }


}
