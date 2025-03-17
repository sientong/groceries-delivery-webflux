package com.sientong.groceries.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sientong.groceries.api.request.UpdatePasswordRequest;
import com.sientong.groceries.api.request.UpdateProfileRequest;
import com.sientong.groceries.api.response.UserResponse;
import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
public class UserController {
    private final UserService userService;

    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the profile of the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(
        @Parameter(hidden = true)
        @AuthenticationPrincipal User user
    ) {
        return Mono.just(user)
                .map(UserResponse::fromDomain);
    }

    @Operation(
        summary = "Update current user profile",
        description = "Update the profile of the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully updated user profile"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/me")
    public Mono<UserResponse> updateProfile(
        @Parameter(hidden = true)
        @AuthenticationPrincipal User currentUser,
        @Parameter(description = "Updated profile details", required = true)
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(currentUser.getId(), request)
                .map(UserResponse::fromDomain);
    }

    @Operation(
        summary = "Update current user password",
        description = "Update the password of the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Successfully updated password"),
        @ApiResponse(responseCode = "400", description = "Invalid input or current password incorrect"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updatePassword(
        @Parameter(hidden = true)
        @AuthenticationPrincipal User currentUser,
        @Parameter(description = "Password update request", required = true)
        @Valid @RequestBody UpdatePasswordRequest request
    ) {
        return userService.updatePassword(currentUser.getId(), request);
    }
}
