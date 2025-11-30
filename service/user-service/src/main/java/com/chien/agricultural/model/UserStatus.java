package com.chien.agricultural.model;

public enum UserStatus {
    ACTIVE,     // Đang hoạt động
    PENDING,    // Chờ duyệt (nếu có)
    BANNED,     // Bị cấm
    LOCKED      // Tạm khóa (do nhập sai nhiều lần...)
}
