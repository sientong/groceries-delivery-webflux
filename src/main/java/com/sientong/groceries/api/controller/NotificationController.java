package com.sientong.groceries.api.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sientong.groceries.api.response.NotificationResponse;
import com.sientong.groceries.domain.notification.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(
        summary = "Get user notifications",
        description = "Retrieve all notifications for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Flux<NotificationResponse> getUserNotifications(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.getUserNotifications(userDetails.getUsername())
                .map(NotificationResponse::fromDomain);
    }

    @Operation(
        summary = "Stream user notifications",
        description = "Stream real-time notifications for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully established notification stream"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationResponse> streamUserNotifications(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.getUserNotifications(userDetails.getUsername())
                .mergeWith(notificationService.getUserNotificationStream(userDetails.getUsername()))
                .map(NotificationResponse::fromDomain);
    }

    @Operation(
        summary = "Get unread notifications",
        description = "Retrieve all unread notifications for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/unread")
    public Flux<NotificationResponse> getUnreadNotifications(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.getUserNotifications(userDetails.getUsername())
                .filter(notification -> !notification.isRead())
                .map(NotificationResponse::fromDomain);
    }

    @Operation(
        summary = "Get unread notification count",
        description = "Get the count of unread notifications for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved unread count"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/unread/count")
    public Mono<Long> getUnreadNotificationCount(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.getUnreadNotificationCount(userDetails.getUsername());
    }

    @Operation(
        summary = "Mark notification as read",
        description = "Mark a specific notification as read"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully marked notification as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{notificationId}/read")
    public Mono<NotificationResponse> markNotificationAsRead(
        @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.markNotificationAsRead(notificationId)
                .map(NotificationResponse::fromDomain);
    }
}
