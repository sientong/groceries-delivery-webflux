package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRepository;
import com.sientong.groceries.infrastructure.persistence.entity.UserEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final ReactiveUserRepository userRepository;

    @Override
    public Mono<User> save(User user) {
        return userRepository.save(UserEntity.fromDomain(user))
                .map(UserEntity::toDomain);
    }

    @Override
    public Mono<User> findById(String id) {
        return userRepository.findById(id)
                .map(UserEntity::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::toDomain);
    }
}
