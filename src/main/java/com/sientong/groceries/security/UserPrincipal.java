package com.sientong.groceries.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPrincipal implements UserDetails {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String address;
    private String phone;
    private boolean enabled;

    public static UserPrincipal fromDomain(User user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .address(user.getAddress())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
