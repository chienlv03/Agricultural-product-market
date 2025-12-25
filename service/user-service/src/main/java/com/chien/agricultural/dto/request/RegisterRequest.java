package com.chien.agricultural.dto.request;

import com.chien.agricultural.model.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RegisterRequest {
    private String phoneNumber;
    private String otpCode;
    private UserRole userRole; // BUYER hoặc SELLER
}