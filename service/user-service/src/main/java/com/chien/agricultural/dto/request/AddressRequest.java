package com.chien.agricultural.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressRequest {
    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String detailAddress;

    // Thông tin hành chính (để gọi API giao hàng GHN/GHTK sau này)
    private Integer provinceId;
    private String provinceName;
    private Integer districtId;
    private String districtName;
    private String wardCode;
    private String wardName;

    // Tọa độ (Bắt buộc để tính ship)
    private Double latitude;
    private Double longitude;

    private Boolean isDefault;
}
