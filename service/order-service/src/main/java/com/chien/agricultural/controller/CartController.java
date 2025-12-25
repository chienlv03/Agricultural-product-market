package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.AddToCartRequest;
import com.chien.agricultural.dto.response.CartItemResponse;
import com.chien.agricultural.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getMyCart() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody AddToCartRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addToCart(userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCart(@RequestBody AddToCartRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateQuantity(userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String productId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok().build();
    }
}
