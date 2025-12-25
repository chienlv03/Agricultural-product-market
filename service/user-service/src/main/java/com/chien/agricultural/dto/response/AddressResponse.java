package com.chien.agricultural.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AddressResponse {
    private String id;
    private String userId;

    private String recipientName;
    private String phone;
    private String detailAddress;
    private Integer provinceId;
    private String provinceName;
    private Integer districtId;
    private String districtName;
    private String wardCode;
    private String wardName;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault;
}
