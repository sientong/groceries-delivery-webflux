package com.sientong.groceries.domain.user;

import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Mono<User> createUser(User user) {
        if (user == null) {
            return Mono.error(() -> new IllegalArgumentException("User cannot be null"));
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Email cannot be null or empty"));
        }

        return userRepository.findByEmail(user.getEmail())
                .flatMap(existingUser -> Mono.<User>error(() ->
                    new IllegalArgumentException("Email already registered")
                ))
                .switchIfEmpty(userRepository.save(user)
                        .flatMap(savedUser -> notificationService.createNotification(
                                savedUser.getId(),
                                "Welcome to Groceries Delivery",
                                "Thank you for registering! Start shopping now and get your groceries delivered to your doorstep.",
                                NotificationType.USER_REGISTERED,
                                savedUser.getId()
                        ).thenReturn(savedUser)));
    }

    public Mono<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Email cannot be null or empty"));
        }
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(() ->
                    new IllegalArgumentException("User not found with email: " + email)
                ));
    }

    public Mono<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                    new IllegalArgumentException("User not found with ID: " + id)
                ));
    }
}
