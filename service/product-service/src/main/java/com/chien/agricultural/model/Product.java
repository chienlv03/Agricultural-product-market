package com.chien.agricultural.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    @TextIndexed // Hỗ trợ tìm kiếm Full-text search
    private String name;

    @Indexed(unique = true)
    private String slug;

    private String description;

    @Field("category_ids")
    @Indexed
    private List<String> categoryIds;

    @Indexed
    private List<String> tags;

    // Media
    private String thumbnail;
    private List<String> images;
    private List<String> videos;

    // Embedded Seller Info (Lưu đè để đọc nhanh)
    private SellerInfo seller;

    // Giá cơ bản (nếu không có biến thể)
    private Double price;
    @Field("original_price")
    private Double originalPrice;
    private String unit;
    private Integer availableQuantity;

    // Biến thể (1kg, 2kg...)
    private List<ProductVariant> variants;

    // Thuộc tính đặc thù nông sản
    private ProductAttribute attributes;

    // Chỉ số hiệu suất (Dùng cho sorting)
    @Field("rating_average")
    private Double ratingAverage = 0.0;
    @Field("review_count")
    private Integer reviewCount = 0;
    @Field("sold_count")
    private Integer soldCount = 0;
    @Field("view_count")
    private Integer viewCount = 0;

    // Trạng thái
    @Indexed
    private ProductStatus status;
    @Field("is_featured")
    private Boolean isFeatured = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant updatedAt;
}
