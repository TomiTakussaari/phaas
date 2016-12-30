package com.github.tomitakussaari.phaas.api;


import com.github.tomitakussaari.phaas.model.Tokens.CreateTokenRequest;
import com.github.tomitakussaari.phaas.model.Tokens.ParseTokenRequest;
import com.github.tomitakussaari.phaas.user.PhaasUser;
import com.github.tomitakussaari.phaas.util.JwtHelper;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/data-protection")
public class DataProtectionApi {

    private final JwtHelper jwtHelper = new JwtHelper();

    @ApiOperation(value = "Creates token with given claims (jwt)", consumes = "application/json")
    @RequestMapping(method = RequestMethod.POST, path = "/token")
    public String generateJWT(@RequestBody CreateTokenRequest createRequest, @ApiIgnore @AuthenticationPrincipal PhaasUser userDetails) {
        return jwtHelper.generate(userDetails, createRequest.getClaims());
    }

    @ApiOperation(value = "Verifies token and returns claims (jwt)", consumes = "application/json")
    @RequestMapping(method = RequestMethod.PUT, path = "/token")
    public Map<String, Object> parseJwt(@RequestBody ParseTokenRequest parseRequest, @ApiIgnore @AuthenticationPrincipal PhaasUser userDetails) {
        return jwtHelper.verifyAndGetClaims(userDetails, parseRequest.getToken(), parseRequest.getRequiredClaims().orElseGet(Collections::emptyMap));
    }
}
