package com.chien.agricultural.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seller_profiles", schema = "user_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SellerProfile {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "seller_id")
    private String sellerId; // PK trùng với User ID

    // Kỹ thuật Shared Primary Key: MapsId sẽ lấy ID của user gán vào userId ở trên
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "seller_id")
    @JsonIgnore
    private User user;

    // --- KYC ---
    @Column(name = "id_card_front", columnDefinition = "TEXT")
    private String idCardFront;

    @Column(name = "id_card_back", columnDefinition = "TEXT")
    private String idCardBack;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    // --- Thông tin vườn ---
    @Column(name = "farm_name", nullable = false, length = 200)
    private String farmName;

    @Column(name = "farm_description", columnDefinition = "TEXT")
    private String farmDescription;

    @Column(name = "farm_address", columnDefinition = "TEXT")
    private String farmAddress;

    // Mảng String trong PostgreSQL (text[]) -> List<String> trong Java
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "farm_photos", columnDefinition = "text[]")
    private List<String> farmPhotos;

    // Tọa độ vườn
    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;

    // JSONB trong PostgreSQL -> List<Object> trong Java
    // Hibernate 6 hỗ trợ native JSON mapping cực mạnh
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "certifications", columnDefinition = "jsonb")
    private List<Certification> certifications;
}