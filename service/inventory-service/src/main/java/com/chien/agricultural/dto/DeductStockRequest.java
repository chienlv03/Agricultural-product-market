package com.chien.agricultural.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class DeductStockRequest {
    private String productId;
    private Integer quantity;
}
