package com.sientong.groceries.config;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class ActuatorSecurityConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    public SecurityWebFilterChain actuatorSecurityFilterChain(
            ServerHttpSecurity http,
            @Qualifier("actuatorUserDetailsService") ReactiveUserDetailsService userDetailsService) {
        
        UserDetailsRepositoryReactiveAuthenticationManager authManager = 
            new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder);

        return http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .pathMatchers("/actuator/prometheus").hasRole("ACTUATOR")
                .anyExchange().hasRole("ACTUATOR"))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(httpBasic -> httpBasic
                .authenticationManager(authManager)
                .securityContextRepository(new WebSessionServerSecurityContextRepository()))
            .build();
    }

    @Bean
    @Qualifier("actuatorUserDetailsService")
    public ReactiveUserDetailsService actuatorUserDetailsService() {
        String encodedPassword = passwordEncoder.encode("actuator123");
        log.debug("Encoded password for actuator user: {}", encodedPassword);
        
        UserDetails user = User.builder()
            .username("actuator")
            .password(encodedPassword)
            .roles("ACTUATOR")
            .build();
        return new MapReactiveUserDetailsService(user);
    }
}
