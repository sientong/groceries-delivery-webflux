package com.sientong.groceries.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String token = extractTokenFromRequest(exchange);
        if (token != null) {
            String username = jwtService.validateTokenAndGetUsername(token);
            if (username != null) {
                return userDetailsService.findByUsername(username)
                    .map(userDetails -> {
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                        );
                        SecurityContext securityContext = new SecurityContextImpl();
                        securityContext.setAuthentication(auth);
                        return securityContext;
                    })
                    .flatMap(securityContext -> {
                        exchange.getAttributes().put(SecurityContext.class.getName(), securityContext);
                        return chain.filter(exchange);
                    })
                    .switchIfEmpty(chain.filter(exchange));
            }
        }
        return chain.filter(exchange);
    }

    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
