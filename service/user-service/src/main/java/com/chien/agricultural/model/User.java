package com.chien.agricultural.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", schema = "user_service") // Quan trọng: Mapping đúng schema
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // Để tự động update created_at, updated_at
public class User {

    @Id
    @Column(name = "id", length = 36)
    private String id; // Lưu ý: Đây là UUID String từ Keycloak, không tự sinh!

    @Column(name = "phone", unique = true, nullable = false, length = 15)
    private String phone;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole userRole; // Default là BUYER

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UserStatus status; // Default là ACTIVE

    // --- Audit Fields ---
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relationships ---

    // Một User có nhiều địa chỉ giao hàng
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Tránh vòng lặp vô tận khi serialize JSON
    private List<Address> addresses;

    // Một User (nếu là Seller) sẽ có 1 hồ sơ nông dân
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private SellerProfile sellerProfile;
}