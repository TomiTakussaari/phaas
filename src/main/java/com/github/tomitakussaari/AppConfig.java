package com.github.tomitakussaari;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.tomitakussaari.util.PasswordHasher;
import com.github.tomitakussaari.util.PasswordVerifier;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories
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
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule())
                .registerModule(new AfterburnerModule());
    }
}
