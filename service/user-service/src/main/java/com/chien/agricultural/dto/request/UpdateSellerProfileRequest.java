package com.chien.agricultural.dto.request;

import com.chien.agricultural.model.Certification;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UpdateSellerProfileRequest {
    // Thông tin User cơ bản
    private String fullName;
    private String phone;
    private String email;

    // Thông tin định danh Seller
    private String taxCode;

    // Thông tin Vườn/Nông trại
    private String farmName;
    private String farmDescription;
    private String farmAddress;

    // Tọa độ (Frontend gửi Double, Backend convert sang Point)
    private Double latitude;
    private Double longitude;

    // Chứng chỉ (JSONB)
    private List<Certification> certifications;
}