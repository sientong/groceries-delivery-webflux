package com.sientong.groceries.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import com.sientong.groceries.security.JwtAuthenticationFilter;
import com.sientong.groceries.security.JwtService;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("SELLER")
                    .pathMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("SELLER")
                    .pathMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("SELLER")
                    .pathMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasRole("SELLER")
                    .pathMatchers("/api/v1/orders/**").authenticated()
                    .pathMatchers("/api/v1/notifications/**").authenticated()
                    .pathMatchers("/api/v1/users/**").hasRole("ADMIN")
                    .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationFilter(jwtService, userDetailsService), 
                           SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
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
