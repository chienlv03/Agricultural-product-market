package com.chien.agricultural.entity;

import jakarta.persistence.*;
import lombok.*;

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
public class Order {
    @Id
    private String id;

    private String buyerId;
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

    private Instant createdAt;
    private Instant updatedAt;
}