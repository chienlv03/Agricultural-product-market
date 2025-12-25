package com.chien.agricultural.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private String sku;
    private Double weight;
}
