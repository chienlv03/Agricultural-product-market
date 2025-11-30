package com.chien.agricultural.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginRequest {
    private String phoneNumber;
    private String otpCode;
}