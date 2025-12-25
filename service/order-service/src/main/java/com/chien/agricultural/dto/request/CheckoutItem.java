package com.chien.agricultural.dto.request;

import lombok.Data;

@Data
public class CheckoutItem {
    private String productId;
    private Integer quantity; // <--- SỐ LƯỢNG MUA NẰM Ở ĐÂY
}
