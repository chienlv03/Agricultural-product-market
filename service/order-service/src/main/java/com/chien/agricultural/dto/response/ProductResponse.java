package com.chien.agricultural.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private String thumbnail;
    private SellerProfileResponse sellerProfileResponse;
    private Integer availableQuantity;
    private String unit;
    private String sku;
    private Double weight;
}
