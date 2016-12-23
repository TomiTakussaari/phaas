package com.github.tomitakussaari.phaas.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.ApiUsersService;
import com.github.tomitakussaari.phaas.user.PhaasUserDetails;
import io.swagger.annotations.ApiOperation;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UsersApi {

    private final ApiUsersService apiUsersService;
    private final Environment environment;


    @ApiOperation(value = "Returns information about current user")
    @Secured({ApiUsersService.USER_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.GET, produces = "application/json", path = "/me")
    public PublicUser whoAmI(@ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        return toPublicUser(userDetails);
    }

    @ApiOperation(value = "Creates new user, only usable by admins")
    @Secured({ApiUsersService.ADMIN_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public CreateUserResponse createUser(@RequestBody CreateUserRequest request) {
        rejectIfUserDatabaseIsImmutable();
        return new CreateUserResponse(apiUsersService.createUser(request.getUserName(), request.getAlgorithm(), request.getRoles(), request.getSharedSecretForSigningCommunication()));
    }

    @ApiOperation(value = "Lists all users, only usable by admins")
    @Secured({ApiUsersService.ADMIN_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<PublicUser> listUsers() {
        return apiUsersService.findAll().stream().map(this::toPublicUser).collect(toList());
    }

    @ApiOperation(value = "Deletes user, only usable by admins")
    @Secured({ApiUsersService.ADMIN_ROLE_VALUE})
    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json", path = "/{userName}")
    public void deleteUser(@PathVariable("userName") String userName) {
        rejectIfUserDatabaseIsImmutable();
        apiUsersService.deleteUser(userName);
    }

    @ApiOperation(value = "Creates new protectionscheme and makes it active")
    @RequestMapping(method = RequestMethod.POST, path = "/me/scheme", consumes = "application/json")
    public void newProtectionScheme(@RequestBody ProtectionSchemeRequest request, @ApiIgnore @AuthenticationPrincipal PhaasUserDetails userDetails) {
        rejectIfUserDatabaseIsImmutable();
        apiUsersService.newProtectionScheme(userDetails.getUsername(), request.getAlgorithm(), userDetails.findCurrentUserPassword(), request.isRemoveOldSchemes());
    }

    private PublicUser toPublicUser(PhaasUserDetails userDetails) {
        return PublicUser.builder()
                .userName(userDetails.getUsername())
                .currentProtectionScheme(userDetails.activeProtectionScheme().toPublicScheme())
                .supportedProtectionSchemes(userDetails.protectionSchemes().stream().map(DataProtectionScheme::toPublicScheme).collect(toList()))
                .roles(userDetails.getAuthorities().stream().map(authority -> ApiUsersService.ROLE.fromDbValue(authority.getAuthority())).collect(toList()))
                .build();
    }

    private void rejectIfUserDatabaseIsImmutable() {
        if (environment.getProperty("immutable.users.db", Boolean.class)) {
            throw new OperationIsNotAvailableException("operation is not available");
        }
    }

    @Data
    @Builder
    static class PublicUser {
        private final String userName;
        private final List<ApiUsersService.ROLE> roles;
        private final DataProtectionScheme.PublicProtectionScheme currentProtectionScheme;
        private final List<DataProtectionScheme.PublicProtectionScheme> supportedProtectionSchemes;
    }

    @RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
    @Getter
    public class ProtectionSchemeRequest {
        @NonNull
        private final PasswordEncodingAlgorithm algorithm;
        private final boolean removeOldSchemes;
    }

    @RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
    @Getter
    static class CreateUserRequest {
        @NonNull
        private final String userName;
        @NonNull
        private final PasswordEncodingAlgorithm algorithm;
        @NonNull
        private final List<ApiUsersService.ROLE> roles;
        private final Optional<String> sharedSecretForSigningCommunication;
    }

    @RequiredArgsConstructor
    @Getter
    static class CreateUserResponse {
        private final String generatedPassword;
    }

}
