package com.sientong.groceries.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

import com.sientong.groceries.domain.user.UserRepository;

@Service
@Primary
@RequiredArgsConstructor
@Qualifier("userDetailsService")
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
    
    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmail(username)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
            .map(UserDetailsImpl::new);
    }
}
