package com.chien.agricultural.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String addressId; // Dùng ID để lấy tọa độ tính ship
    private String paymentMethod; // COD, VNPAY
    private String note; // Ghi chú đơn hàng
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private String productId;
        private Integer quantity;
    }
}