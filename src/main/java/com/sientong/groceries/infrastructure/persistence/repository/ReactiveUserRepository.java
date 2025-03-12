package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository extends ReactiveCrudRepository<UserEntity, String> {
    Mono<UserEntity> findByEmail(String email);
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    Mono<UserEntity> findByEmailAndPassword(String email, String password);
}
