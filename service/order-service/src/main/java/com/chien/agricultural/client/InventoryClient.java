package com.chien.agricultural.client;

import com.chien.agricultural.dto.request.DeductStockRequest;
import com.chien.agricultural.orderConfig.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryClient {
    @PostMapping("/api/v1/inventory/deduct-batch")
    void deductStockBatch(@RequestBody List<DeductStockRequest> request);

    @PutMapping("/api/v1/inventory/restore")
    void restoreStock(@RequestBody List<DeductStockRequest> request);
}
