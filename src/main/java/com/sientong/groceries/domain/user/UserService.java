package com.sientong.groceries.domain.user;

import com.sientong.groceries.api.request.UpdatePasswordRequest;
import com.sientong.groceries.api.request.UpdateProfileRequest;
import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
                            "Welcome to Groceries Delivery System!",
                            "Thank you for registering with us.",
                            NotificationType.USER_REGISTERED,
                            savedUser.getId()
                        ).thenReturn(savedUser)));
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

    public Mono<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Email cannot be null or empty"));
        }

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(() ->
                    new IllegalArgumentException("User not found with email: " + email)
                ));
    }

    public Mono<User> updateProfile(String userId, UpdateProfileRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        if (request == null) {
            return Mono.error(() -> new IllegalArgumentException("UpdateProfileRequest cannot be null"));
        }

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(() ->
                    new IllegalArgumentException("User not found with ID: " + userId)
                ))
                .flatMap(user -> {
                    User updatedUser = User.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .password(user.getPassword())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .phone(request.getPhoneNumber())
                            .address(user.getAddress())
                            .role(user.getRole())
                            .build();
                    return userRepository.save(updatedUser);
                });
    }

    public Mono<Void> updatePassword(String userId, UpdatePasswordRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        if (request == null) {
            return Mono.error(() -> new IllegalArgumentException("UpdatePasswordRequest cannot be null"));
        }

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(() ->
                    new IllegalArgumentException("User not found with ID: " + userId)
                ))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        return Mono.error(() -> new IllegalArgumentException("Current password is incorrect"));
                    }

                    User updatedUser = User.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .password(passwordEncoder.encode(request.getNewPassword()))
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phone(user.getPhone())
                            .address(user.getAddress())
                            .role(user.getRole())
                            .build();
                    return userRepository.save(updatedUser)
                            .flatMap(savedUser -> notificationService.createNotification(
                                savedUser.getId(),
                                "Password Updated",
                                "Your password has been successfully updated.",
                                NotificationType.PASSWORD_RESET,
                                savedUser.getId()
                            ))
                            .then();
                });
    }
}
