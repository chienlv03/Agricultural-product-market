package com.chien.agricultural.dto.response;

import com.chien.agricultural.entity.OrderItem;
import com.chien.agricultural.entity.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;

    private String buyerId;
    private String sellerId;

    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;

    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;

    private OrderStatus status;

    private String paymentMethod;
    private Boolean isPreOrder;

    private List<OrderItemResponse> items;

    private Instant createdAt;
}
