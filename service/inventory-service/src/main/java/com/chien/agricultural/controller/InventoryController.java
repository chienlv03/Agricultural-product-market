package com.chien.agricultural.controller;

import com.chien.agricultural.dto.DeductStockRequest;
import com.chien.agricultural.dto.InitStockRequest;
import com.chien.agricultural.dto.InventoryStockResponse;
import com.chien.agricultural.entity.Inventory;
import com.chien.agricultural.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    @PostMapping("/deduct-batch")
    public ResponseEntity<Void> deductStockBatch(@RequestBody List<DeductStockRequest> requests) {
        inventoryService.deductStockBatch(requests);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/restore")
    public ResponseEntity<Void> restoreStock(@RequestBody List<DeductStockRequest> items) {
        inventoryService.restoreStock(items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batch-stock")
    public ResponseEntity<List<InventoryStockResponse>> getBatchStock(@RequestParam List<String> productIds) {

        List<InventoryStockResponse> response =
                inventoryService.findByProductIdIn(productIds);

        return ResponseEntity.ok(response);
    }
}