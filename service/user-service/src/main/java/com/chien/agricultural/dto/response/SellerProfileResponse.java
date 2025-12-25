package com.chien.agricultural.dto.response;

import com.chien.agricultural.model.Certification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
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
    private List<Certification> certifications;
}
