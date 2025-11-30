package com.chien.agricultural.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductVariant {
    private String variantId;
    private String sku;
    private String name; // Ví dụ: "Túi 5kg"
    private Double price;
    private Double originalPrice;
    private Integer stockQuantity; // Chỉ lưu số lượng cho hiển thị
    private List<String> images;
}
