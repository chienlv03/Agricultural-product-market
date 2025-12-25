package com.chien.agricultural.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private String productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
}
