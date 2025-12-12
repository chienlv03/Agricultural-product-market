package com.chien.agricultural.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items; // Danh sách sản phẩm mua
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String paymentMethod; // COD, VNPAY
}
