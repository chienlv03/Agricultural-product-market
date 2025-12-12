package com.chien.agricultural.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "inventories", schema = "inventory_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", unique = true, nullable = false)
    private String productId; // ID từ Product Service (MongoDB)

    @Column(nullable = false)
    private Integer quantity; // Tổng sản lượng dự kiến (VD: 50kg)

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0; // Số lượng khách đã đặt cọc (VD: 10kg)

    @Column(name = "harvest_date")
    private Instant harvestDate; // Ngày thu hoạch dự kiến (Để check logic pre-order)

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}