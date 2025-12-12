package com.chien.agricultural.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders", schema = "order_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    private String id; // Sẽ tự generate mã đơn đẹp (VD: ORD-timestamp)

    private String userId;
    private String sellerId;

    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;

    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // Enum: PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED

    private String paymentMethod;
    private Boolean isPreOrder;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}