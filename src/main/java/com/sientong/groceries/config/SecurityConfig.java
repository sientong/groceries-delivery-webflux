package com.sientong.groceries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

import com.sientong.groceries.security.JwtAuthenticationFilter;
import com.sientong.groceries.security.JwtService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtService jwtService;

    private static final String[] PUBLIC_PATHS = {
        "/api/v1/auth/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/actuator/health",
        "/ws/**"  // Allow WebSocket connections without authentication
    };

    private static final String[] GET_ONLY_PUBLIC_PATHS = {
        "/api/v1/products/**",
        "/api/v1/categories/**"  
    };

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(securityContextRepository())
                .authorizeExchange(exchanges -> exchanges
                    .pathMatchers(PUBLIC_PATHS).permitAll()
                    .pathMatchers(HttpMethod.GET, GET_ONLY_PUBLIC_PATHS).permitAll()
                    .pathMatchers("/api/v1/cart/**").hasRole("CUSTOMER")
                    .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationFilter(jwtService, userDetailsService), 
                           SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authManager = 
            new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder());
        return authManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
