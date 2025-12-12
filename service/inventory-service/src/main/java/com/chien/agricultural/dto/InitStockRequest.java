package com.chien.agricultural.dto;

import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class InitStockRequest {
    private String productId;
    private Integer quantity;
    private Instant harvestDate;
}
