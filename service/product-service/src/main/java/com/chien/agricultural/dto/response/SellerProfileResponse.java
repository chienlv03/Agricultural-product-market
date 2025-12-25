package com.chien.agricultural.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SellerProfileResponse {
    private String sellerId;
    private String phone;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String idCardFront;
    private String idCardBack;
    private String taxCode;
    private String farmName;
    private String farmDescription;
    private String farmAddress;
    private List<String> farmPhotos;
    private Double latitude;
    private Double longitude;
}
