package com.chien.agricultural.dto.request;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class InitStockRequest {
    private String productId;   // ID sản phẩm vừa tạo
    private Integer quantity;   // Số lượng
    private Instant harvestDate; // Ngày thu hoạch (để check logic pre-order)
}