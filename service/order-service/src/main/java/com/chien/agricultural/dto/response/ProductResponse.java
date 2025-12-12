package com.chien.agricultural.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private String thumbnail;
    private SellerDto seller; // Cần lấy ID người bán để tách đơn

    @Data
    public static class SellerDto {
        private String id;
        private String name;
    }
}
