package com.chien.agricultural.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerInfo {
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
