package com.github.tomitakussaari.phaas.api;


import com.github.tomitakussaari.phaas.model.JWT.JwtCreateRequest;
import com.github.tomitakussaari.phaas.model.JWT.JwtParseRequest;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
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

    @ApiOperation(value = "Creates token with given claims (jwt)", consumes = "application/json")
    @RequestMapping(method = RequestMethod.POST, path = "/token")
    public String generateJWT(@RequestBody JwtCreateRequest createRequest, @ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return JwtHelper.generate(userDetails, createRequest.getClaims());
    }

    @ApiOperation(value = "Verifies token and returns claims (jwt)", consumes = "application/json")
    @RequestMapping(method = RequestMethod.PUT, path = "/token")
    public Map<String, Object> parseJwt(@RequestBody JwtParseRequest parseRequest, @ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return JwtHelper.verifyAndGetClaims(userDetails, parseRequest.getToken(), parseRequest.getRequiredClaims().orElseGet(() -> Collections.EMPTY_MAP));
    }
}
