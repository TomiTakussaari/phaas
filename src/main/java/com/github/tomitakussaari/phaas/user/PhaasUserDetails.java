package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import com.github.tomitakussaari.phaas.model.ProtectionSchemeNotFoundException;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class PhaasUserDetails implements UserDetails {

    private final UserDTO userDTO;
    private final List<UserConfigurationDTO> configurations;

    public ProtectionScheme activeProtectionScheme() {
        return configurations.stream().filter(UserConfigurationDTO::isActive).findFirst()
                .map(toProtectionScheme())
                .orElseThrow(() -> new ProtectionSchemeNotFoundException("Unable to find active encryption key for user: " + getUsername()));
    }

    public ProtectionScheme protectionScheme(int id) {
        return configurations.stream().filter(config -> config.getId().equals(id)).findFirst()
                .map(toProtectionScheme())
                .orElseThrow(() -> new ProtectionSchemeNotFoundException("Unable to find encryption key by id: " + id));
    }

    public List<ProtectionScheme> protectionSchemes() {
        return configurations.stream().map(toProtectionScheme()).collect(toList());
    }

    public String findCurrentUserPassword() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    private Function<UserConfigurationDTO, ProtectionScheme> toProtectionScheme() {
        return activeConfig -> new ProtectionScheme(activeConfig.getId(), activeConfig.getAlgorithm(), activeConfig.getDataProtectionKey());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDTO.roles().stream().map(SimpleGrantedAuthority::new).collect(toList());
    }

    @Override
    public String getPassword() {
        return userDTO.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return userDTO.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
