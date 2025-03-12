package com.sientong.groceries.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating order delivery information")
public class UpdateDeliveryInfoRequest {
    @Schema(description = "Delivery address", example = "123 Main St, City, Country", required = true)
    @NotBlank(message = "Address cannot be blank")
    private String address;

    @Schema(description = "Contact phone number", example = "+1234567890", required = true)
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "Delivery tracking number", example = "TRK123456789")
    private String trackingNumber;

    @Schema(description = "Estimated delivery time", example = "2025-03-13T14:30:00")
    @NotNull(message = "Estimated delivery time cannot be null")
    private LocalDateTime estimatedDeliveryTime;

    @Schema(description = "Additional delivery notes", example = "Please ring the doorbell")
    private String deliveryNotes;
}
