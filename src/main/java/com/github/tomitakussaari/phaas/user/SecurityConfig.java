package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.util.JsonHelper;
import io.dropwizard.servlets.ThreadNameFilter;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.tomitakussaari.phaas.user.SecurityConfig.AuditAndLoggingFilter.X_REQUEST_ID;
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

    private FilterRegistrationBean filterRegistration(Filter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(0);
        return registrationBean;
    }

    @ControllerAdvice
    public static class HmacCalculationAdvice implements ResponseBodyAdvice<Object> {
        public static final String X_RESPONSE_SIGN = "X-Response-Signature";

        @Override
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            return true;
        }

        @Override
        public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                      Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                      ServerHttpRequest request, ServerHttpResponse response) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && ((PhaasUserDetails) authentication.getPrincipal()).communicationSigningKey() != null) {
                PhaasUserDetails userDetails = (PhaasUserDetails) authentication.getPrincipal();
                String responseTime = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
                String bodyAsString = JsonHelper.serialize(body);
                String finalHmac = calculateSignature(response.getHeaders().getFirst(X_REQUEST_ID), userDetails.communicationSigningKey(), responseTime, bodyAsString);
                response.getHeaders().add("date", responseTime);
                response.getHeaders().add(X_RESPONSE_SIGN, finalHmac);
            }
            return body;
        }

        public static String calculateSignature(String requestId, String signKey, String responseTime, String bodyAsString) {
            String bodyHmac = HmacUtils.hmacSha256Hex(signKey, bodyAsString);
            String xApiRequestIdHmac = HmacUtils.hmacSha256Hex(bodyHmac, requestId);
            return HmacUtils.hmacSha256Hex(xApiRequestIdHmac, responseTime);
        }
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
            String path = request.getRequestURI().substring(request.getContextPath().length());
            if (request.getQueryString() != null) {
                path = path + "?" + request.getQueryString();
            }
            return path;
        }

        String getRemoteIp(HttpServletRequest request) {
            return ofNullable(request.getHeader(X_REAL_IP))
                    .orElseGet(() -> ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr()));
        }

    }

}
