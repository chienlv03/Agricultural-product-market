package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.CreateOrderRequest;
import com.chien.agricultural.sevice.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<List<String>> createOrder(@RequestBody CreateOrderRequest request) {
        // TODO: Lấy userId từ Token (SecurityContext)
        // String userId = SecurityContextHolder....
        String userId = "test-user-id"; // Fix cứng để test trước

        List<String> orderIds = orderService.createOrder(request, userId);
        return ResponseEntity.ok(orderIds);
    }
}