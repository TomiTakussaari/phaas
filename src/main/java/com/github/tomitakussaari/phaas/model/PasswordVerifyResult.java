package com.github.tomitakussaari.phaas.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class PasswordVerifyResult {
    @ApiModelProperty(notes = "new password hash if new protection scheme is active")
    private final Optional<String> upgradedHash;
    private final boolean valid;
}
