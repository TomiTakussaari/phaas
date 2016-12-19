package com.github.tomitakussaari.phaas;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.tomitakussaari.phaas.util.PasswordHasher;
import com.github.tomitakussaari.phaas.util.PasswordVerifier;
import com.zaxxer.hikari.HikariDataSource;
import io.dropwizard.servlets.ThreadNameFilter;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

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
    public FilterRegistrationBean auditAndLoggingFilter() {
        return filterRegistration(new AuditAndLoggingFilter());
    }

    @Bean
    public FilterRegistrationBean requestPathAsThreadNameFilter() {
        return filterRegistration(new ThreadNameFilter());
    }

    private  FilterRegistrationBean filterRegistration(Filter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(0);
        return registrationBean;
    }

    public static class AuditAndLoggingFilter extends OncePerRequestFilter {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_REAL_IP = "X-Real-IP";

        public static final String MDC_REQUEST_ID = "requestId";
        public static final String MDC_PATH = "path";
        public static final String MDC_IP = "ip";
        public static final String MDC_USER = "user";

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            final String requestId = ofNullable(trimToNull(request.getHeader(X_REQUEST_ID))).orElse(UUID.randomUUID().toString());
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                response.setHeader(X_REQUEST_ID, requestId);
                MDC.put(MDC_PATH, getPath(request));
                MDC.put(MDC_IP, getRemoteIp(request));
                MDC.put(MDC_REQUEST_ID, requestId);
                MDC.put(MDC_USER, authentication != null ? authentication.getName() : "");
                filterChain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        }

        @Override
        public void destroy() {
            //nothing to do
        }

        String getPath(HttpServletRequest request) {
            String path =  request.getRequestURI().substring(request.getContextPath().length());
            if(request.getQueryString() != null) {
                path = path + "?"+request.getQueryString();
            }
            return path+ " "+request.getMethod();
        }

        String getRemoteIp(HttpServletRequest request) {
            return ofNullable(request.getHeader(X_REAL_IP))
                    .orElseGet(() -> ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr()));
        }

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
