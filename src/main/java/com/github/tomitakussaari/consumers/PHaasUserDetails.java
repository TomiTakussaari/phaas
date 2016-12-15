package com.github.tomitakussaari.consumers;

import com.github.tomitakussaari.model.ProtectionScheme;
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

    public ProtectionScheme activeProtectionScheme() {
        return configurations.stream().filter(ApiConsumerConfigurationRepository.ApiConsumerConfiguration::isActive).findFirst()
                .map(activeConfig -> new ProtectionScheme(activeConfig.getId(), activeConfig.getAlgorithm(), encryptionKey(activeConfig)))
                .orElseThrow(() -> new RuntimeException("Unable to find active encryption key"));
    }

    private String encryptionKey(ApiConsumerConfigurationRepository.ApiConsumerConfiguration activeConfig) {
        String salt = activeConfig.getEnc_key().split(":::")[0];
        String encryptedVal = activeConfig.getEnc_key().split(":::")[1];
        TextEncryptor decryptor = Encryptors.text(findPassword(), salt);
        return decryptor.decrypt(encryptedVal);
    }

    private String findPassword() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    public ProtectionScheme protectionScheme(int id) {
        return configurations.stream().filter(config -> config.getId().equals(id)).findFirst()
                .map(activeConfig -> new ProtectionScheme(activeConfig.getId(), activeConfig.getAlgorithm(), encryptionKey(activeConfig)))
                .orElseThrow(() -> new RuntimeException("Unable to find encryption key by id: " + id));

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
