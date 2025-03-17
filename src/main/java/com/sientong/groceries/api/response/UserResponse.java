package com.sientong.groceries.api.response;

import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String address;
    private String phoneNumber;
    private String createdAt;
    private String updatedAt;

    public static UserResponse fromDomain(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .address(user.getAddress())
            .phoneNumber(user.getPhone())
            .build();
    }
}
