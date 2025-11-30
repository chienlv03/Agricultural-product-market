package com.chien.agricultural.model;

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

    private Instant harvestDate; // Ngày thu hoạch
    private Integer expiryDays;  // Hạn sử dụng (ngày)
    private String preservation; // Hướng dẫn bảo quản
    private List<Certification> certifications;
}
