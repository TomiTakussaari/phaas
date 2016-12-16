package com.github.tomitakussaari.consumers;

import com.github.tomitakussaari.model.ProtectionScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PhaasUserDetails implements UserDetails {

    private final ApiConsumersRepository.ApiConsumer apiConsumer;
    private final List<ApiConsumerConfigurationRepository.ApiConsumerConfiguration> configurations;

    public ProtectionScheme activeProtectionScheme() {
        return configurations.stream().filter(ApiConsumerConfigurationRepository.ApiConsumerConfiguration::isActive).findFirst()
                .map(toProtectionScheme())
                .orElseThrow(() -> new RuntimeException("Unable to find active encryption key"));
    }


    private String findPassword() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    public ProtectionScheme protectionScheme(int id) {
        return configurations.stream().filter(config -> config.getId().equals(id)).findFirst()
                .map(toProtectionScheme())
                .orElseThrow(() -> new RuntimeException("Unable to find encryption key by id: " + id));

    }

    private Function<ApiConsumerConfigurationRepository.ApiConsumerConfiguration, ProtectionScheme> toProtectionScheme() {
        return activeConfig -> new ProtectionScheme(activeConfig.getId(), activeConfig.getAlgorithm(), activeConfig.decryptDataProtectionKey(findPassword()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return apiConsumer.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return apiConsumer.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return apiConsumer.getUserName();
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
