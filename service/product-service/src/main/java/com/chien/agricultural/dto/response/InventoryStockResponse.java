package com.chien.agricultural.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryStockResponse {
    private String productId;
    private Integer availableQuantity;
}
