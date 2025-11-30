package com.chien.agricultural.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateSellerProfileRequest {
    private String fullName; // Cập nhật luôn tên hiển thị của User
    private String farmName;
    private String farmDescription;
    private String farmAddress;
    // Tạm thời chưa xử lý tọa độ phức tạp, nhận text trước
}