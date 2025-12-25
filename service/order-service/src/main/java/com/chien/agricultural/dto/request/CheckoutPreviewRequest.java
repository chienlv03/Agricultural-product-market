package com.chien.agricultural.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutPreviewRequest {
    private String addressId;
    private List<CheckoutItem> items; // Danh sách sản phẩm muốn mua
}
