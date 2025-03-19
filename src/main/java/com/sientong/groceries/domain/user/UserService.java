package com.sientong.groceries.domain.user;

import org.springframework.stereotype.Service;

import com.sientong.groceries.api.request.UpdatePasswordRequest;
import com.sientong.groceries.api.request.UpdateProfileRequest;

import reactor.core.publisher.Mono;

@Service
public interface UserService {
    Mono<User> createUser(User user);
    Mono<User> findById(String id);
    Mono<User> findByEmail(String email);
    Mono<User> updateProfile(String userId, UpdateProfileRequest request);
    Mono<Void> updatePassword(String userId, UpdatePasswordRequest request);
}
