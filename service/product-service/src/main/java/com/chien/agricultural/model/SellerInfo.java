package com.chien.agricultural.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellerInfo {
    private String id; // User ID
    private String name;
    private String avatar;
    private Integer provinceId;
    private String provinceName;
    private Double rating;
}
