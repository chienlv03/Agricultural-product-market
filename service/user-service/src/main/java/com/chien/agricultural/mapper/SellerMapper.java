package com.chien.agricultural.mapper;

import com.chien.agricultural.dto.response.SellerProfileResponse;
import com.chien.agricultural.model.SellerProfile;
import com.chien.agricultural.model.User;

public class SellerMapper {

    private SellerMapper() {
        // utility class
    }

    public static SellerProfileResponse toResponse(User user, SellerProfile profile) {

        return SellerProfileResponse.builder()
                .sellerId(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())

                .farmName(profile != null ? profile.getFarmName() : null)
                .farmAddress(profile != null ? profile.getFarmAddress() : null)
                .farmDescription(profile != null ? profile.getFarmDescription() : null)
                .farmPhotos(profile != null ? profile.getFarmPhotos() : null)
                .idCardFront(profile != null ? profile.getIdCardFront() : null)
                .idCardBack(profile != null ? profile.getIdCardBack() : null)
                .taxCode(profile != null ? profile.getTaxCode() : null)
                .certifications(profile != null ? profile.getCertifications() : null)

                .latitude(profile != null && profile.getLocation() != null
                        ? profile.getLocation().getY()
                        : null)
                .longitude(profile != null && profile.getLocation() != null
                        ? profile.getLocation().getX()
                        : null)

                .build();
    }
}
