package com.github.tomitakussaari.api;

import com.github.tomitakussaari.model.ProtectionScheme;
import com.github.tomitakussaari.user.ApiUsersService;
import com.github.tomitakussaari.user.PhaasUserDetails;
import io.swagger.annotations.ApiOperation;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UsersApi {

    private final ApiUsersService apiUsersService;

    @ApiOperation(value = "Returns information about current user")
    @Secured({ApiUsersService.USER_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public PublicUser whoAmI(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return toPublicUser(userDetails);
    }

    @Secured({ApiUsersService.ADMIN_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public NewUserResponse createUser(@RequestBody NewUserRequest request) {
        String password = RandomStringUtils.randomAlphanumeric(30);
        apiUsersService.createUser(request.getUserName(), request.getAlgorithm(), request.getRoles(), password);
        return new NewUserResponse(password);
    }

    private PublicUser toPublicUser(PhaasUserDetails userDetails) {
        return PublicUser.builder()
                .userName(userDetails.getUsername())
                .currentProtectionScheme(userDetails.activeProtectionScheme().toPublicScheme())
                .supportedProtectionSchemes(userDetails.protectionSchemes().stream().map(ProtectionScheme::toPublicScheme).collect(Collectors.toList()))
                .roles(userDetails.getAuthorities().stream().map(authority -> ApiUsersService.ROLE.fromDbValue(authority.getAuthority())).collect(Collectors.toList()))
                .build();
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
    static class NewUserRequest {
        @NonNull
        private final String userName;
        @NonNull
        private final ProtectionScheme.PasswordEncodingAlgorithm algorithm;
        @NonNull
        private final List<ApiUsersService.ROLE> roles;
    }

    @RequiredArgsConstructor
    @Getter
    static class NewUserResponse {
        private final String generatedPassword;
    }

}
