package com.github.tomitakussaari.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final List<String> UNSECURE_ENDPOINTS = Arrays.asList("/", "/swagger-ui.html", "/webjars/", "/swagger-resources", "/v2/api-docs");

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
        return UNSECURE_ENDPOINTS.stream().noneMatch(endpoint -> httpServletRequest.getServletPath().startsWith(endpoint));
    }
}
