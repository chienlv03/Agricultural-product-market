package com.chien.agricultural.client;

import com.chien.agricultural.dto.request.DeductStockRequest;
import com.chien.agricultural.orderConfig.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryClient {
    @PostMapping("/api/v1/inventory/deduct")
    void deductStock(@RequestBody DeductStockRequest request);
}
