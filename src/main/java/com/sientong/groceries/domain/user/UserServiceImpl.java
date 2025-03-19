package com.sientong.groceries.domain.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sientong.groceries.api.request.UpdatePasswordRequest;
import com.sientong.groceries.api.request.UpdateProfileRequest;
import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Override
    public Mono<User> createUser(User user) {
        if (user == null) {
            return Mono.error(() -> new IllegalArgumentException("User cannot be null"));
        }

        return userRepository.findByEmail(user.getEmail())
                .flatMap(existingUser -> Mono.<User>error(
                        new IllegalArgumentException("Email already exists")
                ))
                .switchIfEmpty(Mono.defer(() -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepository.save(user)
                            .flatMap(savedUser -> notificationService.createNotification(
                                    savedUser.getId(),
                                    "Welcome to Groceries Delivery System!",
                                    "Thank you for registering with us.",
                                    NotificationType.USER_REGISTERED,
                                    savedUser.getId()
                            ).thenReturn(savedUser));
                }));
    }

    @Override
    public Mono<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("User not found with ID: " + id)
                ));
    }

    @Override
    public Mono<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Email cannot be null or empty"));
        }
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("User not found with email: " + email)
                ));
    }

    @Override
    public Mono<User> updateProfile(String userId, UpdateProfileRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        if (request == null) {
            return Mono.error(() -> new IllegalArgumentException("Update request cannot be null"));
        }

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("User not found with ID: " + userId)
                ))
                .flatMap(user -> {
                    if (request.getFirstName() != null) {
                        user.setFirstName(request.getFirstName());
                    }
                    if (request.getLastName() != null) {
                        user.setLastName(request.getLastName());
                    }
                    if (request.getEmail() != null) {
                        user.setEmail(request.getEmail());
                    }
                    if (request.getPhone() != null) {
                        user.setPhone(request.getPhone());
                    }
                    if (request.getAddress() != null) {
                        user.setAddress(request.getAddress());
                    }
                    return userRepository.save(user);
                });
    }

    @Override
    public Mono<Void> updatePassword(String userId, UpdatePasswordRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        if (request == null) {
            return Mono.error(() -> new IllegalArgumentException("Update request cannot be null"));
        }

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("User not found with ID: " + userId)
                ))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        return Mono.error(new IllegalArgumentException("Current password is incorrect"));
                    }
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    return userRepository.save(user);
                })
                .then();
    }
}
