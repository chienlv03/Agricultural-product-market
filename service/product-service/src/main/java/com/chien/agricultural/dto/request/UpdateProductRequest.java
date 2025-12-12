package com.chien.agricultural.dto.request;

import com.chien.agricultural.model.ProductAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateProductRequest {
    private String name;
    private String description;
    private Double price;
    private String unit;
    private List<String> categoryIds;
    private ProductAttribute attributes;

    // Danh sách URL ảnh cũ muốn GIỮ LẠI
    // (Ảnh nào không có trong list này sẽ bị xóa khỏi DB - Logic thay thế)
    private List<String> keepImages;
}
