package com.sientong.groceries.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
}
