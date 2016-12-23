package com.github.tomitakussaari.phaas.user;

import io.dropwizard.servlets.ThreadNameFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final List<String> UNSECURE_ENDPOINTS = Arrays.asList("/swagger-ui.html", "/webjars/", "/swagger-resources", "/v2/api-docs");

    @Autowired
    private ApiUsersService apiUsersService;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.eraseCredentials(false)
                .userDetailsService(apiUsersService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers((RequestMatcher) this::requiresAuthentication).authenticated()
                .and().httpBasic().realmName("phaas")
                .and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private boolean requiresAuthentication(HttpServletRequest httpServletRequest) {
        return !"/".equals(httpServletRequest.getServletPath()) && UNSECURE_ENDPOINTS.stream().noneMatch(httpServletRequest.getServletPath()::startsWith);
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
        public static final String MDC_METHOD = "method";
        public static final String MDC_IP = "ip";
        public static final String MDC_USER = "user";

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            final String requestId = ofNullable(trimToNull(request.getHeader(X_REQUEST_ID))).orElse(UUID.randomUUID().toString());
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                response.setHeader(X_REQUEST_ID, requestId);
                MDC.put(MDC_PATH, getPath(request));
                MDC.put(MDC_METHOD, request.getMethod());
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
            return path;
        }

        String getRemoteIp(HttpServletRequest request) {
            return ofNullable(request.getHeader(X_REAL_IP))
                    .orElseGet(() -> ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr()));
        }

    }

}
