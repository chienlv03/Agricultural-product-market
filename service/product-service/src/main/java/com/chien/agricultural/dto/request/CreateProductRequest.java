package com.chien.agricultural.dto.request;

import com.chien.agricultural.model.ProductAttribute;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CreateProductRequest {
    private String name;
    private String description;
    private Double price;
    private String unit;
    private Integer quantity; // Sản lượng dự kiến (VD: 50kg)
    private List<String> images; // URL ảnh (đã upload qua MinIO trước đó)
    private List<String> categoryIds;
    private ProductAttribute attributes; // Object chứa origin, harvestDate...
}
