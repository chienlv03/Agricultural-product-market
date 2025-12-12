package com.chien.agricultural.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeductStockRequest {
    private String productId;
    private Integer quantity;
}
