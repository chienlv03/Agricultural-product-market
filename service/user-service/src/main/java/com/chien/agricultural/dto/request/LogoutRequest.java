package com.chien.agricultural.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken; // Token dài hạn cần hủy
}