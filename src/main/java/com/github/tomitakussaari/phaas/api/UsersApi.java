package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import com.github.tomitakussaari.phaas.user.ApiUsersService;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import io.swagger.annotations.ApiOperation;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UsersApi {

    private final ApiUsersService apiUsersService;
    private final Environment environment;

    @ApiOperation(value = "Returns information about current user")
    @Secured({ApiUsersService.USER_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public PublicUser whoAmI(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return toPublicUser(userDetails);
    }

    @Secured({ApiUsersService.ADMIN_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public CreateUserResponse createUser(@RequestBody CreateUserRequest request) {
        rejectIfUserDatabaseIsImmutable();
        String password = RandomStringUtils.random(30);
        apiUsersService.createUser(request.getUserName(), request.getAlgorithm(), request.getRoles(), password);
        return new CreateUserResponse(password);
    }

    private PublicUser toPublicUser(PhaasUserDetails userDetails) {
        return PublicUser.builder()
                .userName(userDetails.getUsername())
                .currentProtectionScheme(userDetails.activeProtectionScheme().toPublicScheme())
                .supportedProtectionSchemes(userDetails.protectionSchemes().stream().map(ProtectionScheme::toPublicScheme).collect(toList()))
                .roles(userDetails.getAuthorities().stream().map(authority -> ApiUsersService.ROLE.fromDbValue(authority.getAuthority())).collect(toList()))
                .build();
    }

    private void rejectIfUserDatabaseIsImmutable() {
        if(environment.getProperty("immutable.users.db", Boolean.class)) {
            throw new UnsupportedOperationException("");
        }
    }

    @Data
    @Builder
    static class PublicUser {
        private final String userName;
        private final List<ApiUsersService.ROLE> roles;
        private final ProtectionScheme.PublicProtectionScheme currentProtectionScheme;
        private final List<ProtectionScheme.PublicProtectionScheme> supportedProtectionSchemes;
    }

    @RequiredArgsConstructor
    @Getter
    static class CreateUserRequest {
        @NonNull
        private final String userName;
        @NonNull
        private final ProtectionScheme.PasswordEncodingAlgorithm algorithm;
        @NonNull
        private final List<ApiUsersService.ROLE> roles;
    }

    @RequiredArgsConstructor
    @Getter
    static class CreateUserResponse {
        private final String generatedPassword;
    }

}
