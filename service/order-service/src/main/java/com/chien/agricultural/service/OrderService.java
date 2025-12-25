package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.CreateOrderRequest;
import com.chien.agricultural.dto.response.OrderResponse;
import com.chien.agricultural.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    List<String> createOrder(CreateOrderRequest request, String userId);

    // Lấy danh sách đơn hàng của Seller
    List<OrderResponse> getOrdersBySeller(String sellerId);

    // Cập nhật trạng thái đơn hàng (Duyệt / Hủy / Giao)
    void updateOrderStatus(String sellerId, String orderId, OrderStatus newStatus);

    List<OrderResponse> getOrderByBuyer(String userId);

    void cancelOrder(String userId, String orderId);
}
