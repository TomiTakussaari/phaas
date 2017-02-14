package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.DataProtectionScheme.CryptoData;
import com.github.tomitakussaari.phaas.model.ProtectionSchemeNotFoundException;
import com.github.tomitakussaari.phaas.user.dao.UserConfigurationDTO;
import com.github.tomitakussaari.phaas.user.dao.UserDTO;
import com.github.tomitakussaari.phaas.util.CryptoHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class PhaasUser implements UserDetails {

    private final UserDTO userDTO;
    private final List<UserConfigurationDTO> configurations;
    private final CryptoHelper cryptoHelper;
    @Getter(lazy = true)
    private final CharSequence userPassword = findCurrentUserPassword();


    public void prepareForAsyncUsage() {
        Objects.requireNonNull(getUserPassword(), "user password was not resolved");
    }

    public DataProtectionScheme activeProtectionScheme() {
        return configurations.stream().filter(UserConfigurationDTO::isActive).findFirst()
                .map(config -> config.toProtectionScheme(cryptoHelper))
                .orElseThrow(() -> new ProtectionSchemeNotFoundException("Unable to find active encryption key for user: " + getUsername()));
    }

    public DataProtectionScheme protectionScheme(int id) {
        return configurations.stream().filter(config -> config.getId().equals(id)).findFirst()
                .map(config -> config.toProtectionScheme(cryptoHelper))
                .orElseThrow(() -> new ProtectionSchemeNotFoundException("Unable to find encryption key by id: " + id));
    }

    public List<DataProtectionScheme> protectionSchemes() {
        return configurations.stream().map(config -> config.toProtectionScheme(cryptoHelper)).collect(toList());
    }

    private CharSequence findCurrentUserPassword() {
        return (CharSequence) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    public String communicationSigningKey() {
        return userDTO.getSharedSecretForSigningCommunication();
    }

    public CryptoData currentlyActiveCryptoData() {
        return activeProtectionScheme().cryptoData(getUserPassword());
    }

    public CryptoData cryptoDataForId(int schemeId) {
        return protectionScheme(schemeId).cryptoData(getUserPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDTO.roles().stream().map(SimpleGrantedAuthority::new).collect(toList());
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not to be called");
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
