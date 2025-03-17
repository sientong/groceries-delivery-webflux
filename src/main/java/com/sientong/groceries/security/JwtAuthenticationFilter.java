package com.sientong.groceries.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String token = extractTokenFromRequest(exchange);
        
        if (token == null) {
            return chain.filter(exchange);
        }

        String username = jwtService.validateTokenAndGetUsername(token);
        if (username == null) {
            return chain.filter(exchange);
        }

        return userDetailsService.findByUsername(username)
            .map(userDetails -> {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                return auth;
            })
            .flatMap(authentication -> 
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            )
            .switchIfEmpty(chain.filter(exchange));
    }

    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
