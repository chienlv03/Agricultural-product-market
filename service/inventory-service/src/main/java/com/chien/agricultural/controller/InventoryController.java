package com.chien.agricultural.controller;

import com.chien.agricultural.dto.DeductStockRequest;
import com.chien.agricultural.dto.InitStockRequest;
import com.chien.agricultural.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // API 1: Tạo kho (Product Service gọi sang)
    @PostMapping("/init")
    public ResponseEntity<Void> initStock(@RequestBody InitStockRequest request) {
        inventoryService.initInventory(request.getProductId(), request.getQuantity(), request.getHarvestDate());
        return ResponseEntity.ok().build();
    }

    // API 2: Trừ kho (Order Service gọi sang)
    @PostMapping("/deduct")
    public ResponseEntity<Void> deductStock(@RequestBody DeductStockRequest request) {
        inventoryService.deductStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    // API 3: Check tồn kho (Frontend gọi để hiện: "Còn 5kg")
    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable String productId) {
        // Trả về số lượng khả dụng
        // Bạn tự thêm hàm getAvailableStock trong Service nhé (quantity - reserved)
        return ResponseEntity.ok(0); // Demo
    }
}