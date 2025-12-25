package com.chien.agricultural.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point; // Import chuẩn cho PostGIS
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses", schema = "user_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "detail_address", columnDefinition = "TEXT")
    private String detailAddress;

    // --- Đơn vị hành chính (Lưu ID để tính ship) ---
    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name", length = 50)
    private String provinceName;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district_name", length = 50)
    private String districtName;

    @Column(name = "ward_code", length = 20)
    private String wardCode;

    @Column(name = "ward_name", length = 50)
    private String wardName;

    // --- PostGIS: Tọa độ (Kinh độ, Vĩ độ) ---
    // columnDefinition giúp Hibernate hiểu đây là kiểu hình học Geography
    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}