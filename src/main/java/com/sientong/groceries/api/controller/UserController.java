package com.sientong.groceries.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sientong.groceries.api.request.UpdatePasswordRequest;
import com.sientong.groceries.api.request.UpdateProfileRequest;
import com.sientong.groceries.api.response.UserResponse;
import com.sientong.groceries.domain.user.UserService;
import com.sientong.groceries.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
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
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        if (currentUser == null) {
            log.warn("Unauthorized access attempt to getCurrentUser");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
        }

        log.debug("Getting current user profile: {}", currentUser.getId());
        return userService.findById(currentUser.getId())
                .doOnError(error -> log.error("Error getting user profile: {}", error.getMessage()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .map(UserResponse::fromDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(
        summary = "Update current user profile",
        description = "Update the profile of the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully updated user profile"),
        @ApiResponse(responseCode = "400", description = "Invalid request - validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public Mono<UserResponse> updateProfile(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        if (currentUser == null) {
            log.warn("Unauthorized access attempt to updateProfile");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
        }

        // Validate request manually to ensure proper error handling
        Errors errors = new BeanPropertyBindingResult(request, "updateProfileRequest");
        if (request.getFirstName() != null && request.getFirstName().trim().isEmpty()) {
            errors.rejectValue("firstName", "NotEmpty", "First name cannot be empty");
        }
        if (request.getLastName() != null && request.getLastName().trim().isEmpty()) {
            errors.rejectValue("lastName", "NotEmpty", "Last name cannot be empty");
        }
        if (request.getEmail() != null && request.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "NotEmpty", "Email cannot be empty");
        }

        if (errors.hasErrors()) {
            log.warn("Validation errors in updateProfile request: {}", errors.getAllErrors());
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"));
        }

        log.debug("Updating profile for user: {}", currentUser.getId());
        return userService.updateProfile(currentUser.getId(), request)
                .doOnError(error -> log.error("Error updating user profile: {}", error.getMessage()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .map(UserResponse::fromDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(
        summary = "Update current user password",
        description = "Update the password of the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully updated user password"),
        @ApiResponse(responseCode = "400", description = "Invalid request - validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> updatePassword(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody UpdatePasswordRequest request
    ) {
        if (currentUser == null) {
            log.warn("Unauthorized access attempt to updatePassword");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
        }

        // Validate request manually to ensure proper error handling
        Errors errors = new BeanPropertyBindingResult(request, "updatePasswordRequest");
        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            errors.rejectValue("currentPassword", "NotEmpty", "Current password cannot be empty");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            errors.rejectValue("newPassword", "NotEmpty", "New password cannot be empty");
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            errors.rejectValue("confirmPassword", "NotEmpty", "Confirm password cannot be empty");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "PasswordMismatch", "New password and confirm password do not match");
        }

        if (errors.hasErrors()) {
            log.warn("Validation errors in updatePassword request: {}", errors.getAllErrors());
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"));
        }

        log.debug("Updating password for user: {}", currentUser.getId());
        return userService.updatePassword(currentUser.getId(), request)
                .doOnError(error -> log.error("Error updating user password: {}", error.getMessage()))
                .then() // Convert Mono<Void> to Mono<Void> without switchIfEmpty
                .subscribeOn(Schedulers.boundedElastic());
    }
}
