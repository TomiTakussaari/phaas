package com.github.tomitakussaari.consumers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PHaasUserDetails implements UserDetails {

    private final ApiConsumersRepository.ApiConsumer apiConsumer;
    private final List<ApiConsumerConfigurationRepository.ApiConsumerConfiguration> configurations;
    private final String apiConsumerConfigurationEncKeySalt;

    public String activeEncryptionKey() {
        TextEncryptor decryptor = Encryptors.text(findPassword(), apiConsumerConfigurationEncKeySalt);

        return configurations.stream().filter(ApiConsumerConfigurationRepository.ApiConsumerConfiguration::isActive).findFirst()
                .map(activeConfig -> decryptor.decrypt(activeConfig.getEnc_key()))
                .orElseThrow(() -> new RuntimeException("Unable to find active encryption key"));

    }

    private String findPassword() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    public String encryptionKey(int id) {
        TextEncryptor decryptor = Encryptors.text(findPassword(), apiConsumerConfigurationEncKeySalt);

        return configurations.stream().filter(config -> config.getId().equals(id)).findFirst()
                .map(activeConfig -> decryptor.decrypt(activeConfig.getEnc_key()))
                .orElseThrow(() -> new RuntimeException("Unable to find encryption key by id: "+id));

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return  apiConsumer.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
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
