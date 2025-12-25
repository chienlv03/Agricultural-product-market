package com.chien.agricultural.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String productImage;
    private String sellerId;
    private String sellerName;
    private Integer availableQuantity;
}
