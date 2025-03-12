package com.sientong.groceries.infrastructure.persistence.entity;

import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {
    @Id
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String address;
    private String phone;
    private LocalDateTime createdAt;

    public User toDomain() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .address(address)
                .phone(phone)
                .build();
    }

    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
