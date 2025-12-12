package com.chien.agricultural.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerInfo {
    private String id; // User ID
    private String name;
    private String avatar;
    private Integer provinceId;
    private String provinceName;
    private Double rating;
}
