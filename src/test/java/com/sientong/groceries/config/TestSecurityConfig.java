package com.sientong.groceries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.sientong.groceries.domain.user.UserRole;
import com.sientong.groceries.security.UserPrincipal;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Test security configuration for WebFlux applications.
 * Enables method security and configures security rules for test environment.
 * 
 * Features:
 * - Provides mock ReactiveUserDetailsService for testing
 * - Configures security rules for all endpoints
 * - Disables unnecessary security features for testing
 * - Enables method security for @PreAuthorize support
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class TestSecurityConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        log.debug("Creating test ReactiveUserDetailsService");
        return username -> {
            var principal = UserPrincipal.builder()
                    .id(username)  // Use username as ID for simplicity in tests
                    .email(username)
                    .firstName("Test")
                    .lastName("User")
                    .role(username.contains("admin") ? UserRole.ADMIN : UserRole.CUSTOMER)
                    .enabled(true)
                    .password(passwordEncoder().encode("Password123@"))
                    .build();
            return Mono.just(principal);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.debug("Configuring test security filter chain");

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // User endpoints
                .pathMatchers("/api/v1/users/me/**").authenticated()
                .pathMatchers("/api/v1/users/**").hasRole(UserRole.ADMIN.name())
                
                // Cart endpoints
                .pathMatchers("/api/v1/cart/**").hasRole(UserRole.CUSTOMER.name())
                .pathMatchers("/api/v1/orders/**").hasRole(UserRole.CUSTOMER.name())
                
                // Public endpoints
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("/api/v1/products/**").permitAll()
                .pathMatchers("/api/v1/categories/**").permitAll()
                
                // Swagger endpoints
                .pathMatchers("/swagger-ui.html").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                
                // Health check endpoints
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/actuator/**").hasRole(UserRole.ADMIN.name())
                
                // Deny all other requests by default
                .anyExchange().denyAll()
            )
            .build();
    }
}