package com.chien.agricultural.mapper;

import com.chien.agricultural.dto.response.OrderItemResponse;
import com.chien.agricultural.dto.response.OrderResponse;
import com.chien.agricultural.entity.Order;
import com.chien.agricultural.entity.OrderItem;

import java.util.List;

public class OrderMapper {
    public static OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .isPreOrder(order.getIsPreOrder())
                .items(mapItems(order))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private static List<OrderItemResponse> mapItems(Order order) {
        if (order.getItems() == null) {
            return List.of();
        }

        return order.getItems()
                .stream()
                .map(OrderMapper::toItemResponse)
                .toList();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
