package com.github.tomitakussaari.phaas;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomitakussaari.phaas.util.JsonHelper;
import com.github.tomitakussaari.phaas.util.PasswordHasher;
import com.github.tomitakussaari.phaas.util.PasswordVerifier;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories
@EnableGlobalMethodSecurity(securedEnabled = true)
public class AppConfig {

    @Bean
    @ConfigurationProperties(prefix = "datasource.phaas")
    public DataSource dataSource() {
        return new HikariDataSource();
    }

    @Bean
    public PasswordVerifier passwordVerifyHandler() {
        return new PasswordVerifier();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonHelper.objectMapper;
    }
}
