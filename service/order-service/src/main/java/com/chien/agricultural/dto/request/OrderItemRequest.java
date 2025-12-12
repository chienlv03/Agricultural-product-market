package com.chien.agricultural.dto.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String productId;
    private Integer quantity;
}
