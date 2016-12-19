package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import com.github.tomitakussaari.phaas.model.ProtectionSchemeNotFoundException;
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

    private final PhaasUserRepository.UserDTO userDTO;
    private final List<PhaasUserConfigurationRepository.UserConfigurationDTO> configurations;

    public ProtectionScheme activeProtectionScheme() {
        return configurations.stream().filter(PhaasUserConfigurationRepository.UserConfigurationDTO::isActive).findFirst()
                .map(toProtectionScheme())
                .orElseThrow(() -> new ProtectionSchemeNotFoundException("Unable to find active encryption key for user: " + getUsername()));
    }

    private String findPassword() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    public ProtectionScheme protectionScheme(int id) {
        return configurations.stream().filter(config -> config.getId().equals(id)).findFirst()
                .map(toProtectionScheme())
                .orElseThrow(() -> new ProtectionSchemeNotFoundException("Unable to find encryption key by id: " + id));
    }

    public List<ProtectionScheme> protectionSchemes() {
        return configurations.stream().map(toProtectionScheme()).collect(toList());
    }


    private Function<PhaasUserConfigurationRepository.UserConfigurationDTO, ProtectionScheme> toProtectionScheme() {
        return activeConfig -> new ProtectionScheme(activeConfig.getId(), activeConfig.getAlgorithm(), activeConfig.decryptDataProtectionKey(findPassword()));
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
