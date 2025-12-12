package com.chien.agricultural.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ProductAttribute {
    private String origin; // Đà Lạt

    // GeoJSON Point cho MongoDB
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint gardenLocation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant harvestDate;       // Ngày thu hoạch dự kiến
    private Boolean isPreOrder;        // Có phải hàng đặt trước không?
    private Boolean instantDeliveryOnly; // Chỉ giao hỏa tốc?
    private Integer maxDeliveryRadius; // Bán kính phục vụ tối đa (km). Null = Toàn quốc.
    private Integer expiryDays;  // Hạn sử dụng (ngày)
    private String preservation; // Hướng dẫn bảo quản
    private List<Certification> certifications;
}
