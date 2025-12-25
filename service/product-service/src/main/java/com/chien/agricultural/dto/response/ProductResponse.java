package com.chien.agricultural.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String unit;
    private String thumbnail;
    private Integer availableQuantity;

    private SellerProfileResponse sellerProfileResponse;
    private List<ProductVariantResponse> variants;
}
