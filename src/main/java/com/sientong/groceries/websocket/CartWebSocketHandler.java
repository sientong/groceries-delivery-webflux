package com.sientong.groceries.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sientong.groceries.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartWebSocketHandler implements WebSocketHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .flatMap(message -> {
                    try {
                        // Parse the auth message with type information
                        Map<String, String> authMessage = objectMapper.readValue(
                            message.getPayloadAsText(), 
                            new TypeReference<Map<String, String>>() {}
                        );
                        String token = authMessage.get("token");
                        
                        if (token != null && token.startsWith("Bearer ")) {
                            token = token.substring(7);
                        }

                        // Validate token and get username
                        String userId = jwtService.validateTokenAndGetUsername(token);
                        if (userId != null) {
                            sessions.put(userId, session);
                            
                            // Send confirmation
                            return session.send(Mono.just(
                                session.textMessage("{\"type\":\"connected\",\"message\":\"Successfully connected\"}")
                            ));
                        }
                        
                        // Invalid token
                        return session.send(Mono.just(
                            session.textMessage("{\"type\":\"error\",\"message\":\"Invalid authentication\"}")
                        )).then(session.close());
                        
                    } catch (Exception e) {
                        log.error("Error handling WebSocket message", e);
                        return session.send(Mono.just(
                            session.textMessage("{\"type\":\"error\",\"message\":\"Invalid message format\"}")
                        )).then(session.close());
                    }
                })
                .then()
                .onErrorResume(e -> {
                    log.error("Unhandled error in WebSocket handler", e);
                    return session.send(Mono.just(
                        session.textMessage("{\"type\":\"error\",\"message\":\"Internal server error\"}")
                    )).then(session.close());
                });
    }

    public void sendCartUpdate(String userId, String cartJson) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.send(Mono.just(session.textMessage(cartJson))).subscribe();
        }
    }
}
