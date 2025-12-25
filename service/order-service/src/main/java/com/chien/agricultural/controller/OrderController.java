package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.CreateOrderRequest;
import com.chien.agricultural.dto.response.OrderResponse;
import com.chien.agricultural.entity.OrderStatus;
import com.chien.agricultural.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<List<String>> createOrder(@RequestBody CreateOrderRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<String> orderIds = orderService.createOrder(request, userId);
        return ResponseEntity.ok(orderIds);
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> getOrderBySeller() {
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId));
    }

    // Cập nhật trạng thái (Duyệt/Hủy)
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status
    ) {
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        orderService.updateOrderStatus(sellerId, orderId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/buyer")
    public ResponseEntity<List<OrderResponse>> getOrdersByBuyer() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(orderService.getOrderByBuyer(userId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok().build();
    }

}