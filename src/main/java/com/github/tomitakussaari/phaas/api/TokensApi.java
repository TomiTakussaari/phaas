package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.model.Tokens.CreateTokenRequest;
import com.github.tomitakussaari.phaas.model.Tokens.CreateTokenResponse;
import com.github.tomitakussaari.phaas.model.Tokens.ParseTokenRequest;
import com.github.tomitakussaari.phaas.model.Tokens.ParseTokenResponse;
import com.github.tomitakussaari.phaas.user.PhaasUser;
import com.github.tomitakussaari.phaas.util.JwtHelper;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;

import static com.github.tomitakussaari.phaas.util.AsyncHelper.withName;

@RestController
@RequestMapping("/tokens")
public class TokensApi {

    private final JwtHelper jwtHelper = new JwtHelper();

    @ApiOperation(value = "Creates token with given claims and validity", consumes = "application/json")
    @RequestMapping(method = RequestMethod.POST, path = "/create")
    public DeferredResult<CreateTokenResponse> createJWT(@RequestBody CreateTokenRequest createRequest, @ApiIgnore @AuthenticationPrincipal PhaasUser userDetails) {
        return withName("tokens").toDeferredResult(() -> {
            JwtHelper.CreatedToken createdToken = jwtHelper.generate(userDetails, createRequest.getClaims(), createRequest.getValidityTime());
            return new CreateTokenResponse(createdToken.getId(), createdToken.getToken());
        });
    }

    @ApiOperation(value = "Parses and validates token and returns it's claims", consumes = "application/json")
    @RequestMapping(method = RequestMethod.PUT, path = "/parse")
    public DeferredResult<ParseTokenResponse> parseJwt(@RequestBody ParseTokenRequest parseRequest, @ApiIgnore @AuthenticationPrincipal PhaasUser userDetails) {
        return withName("tokens").toDeferredResult(() -> {
            JwtHelper.ParsedToken parsedToken = jwtHelper.verifyAndGetClaims(userDetails, parseRequest.getToken(), parseRequest.getRequiredClaims().orElseGet(Collections::emptyMap), parseRequest.getMaxAcceptedAge());
            return new ParseTokenResponse(parsedToken.getClaims(), parsedToken.getIssuedAt(), parsedToken.getId());
        });
    }
}
