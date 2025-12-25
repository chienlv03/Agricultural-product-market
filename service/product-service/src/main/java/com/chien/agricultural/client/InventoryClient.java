package com.chien.agricultural.client;

import com.chien.agricultural.dto.response.InventoryStockResponse;
import com.chien.agricultural.productConfig.FeignConfig;
import com.chien.agricultural.dto.request.InitStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Gọi sang "inventory-service" dùng cấu hình FeignConfig (để truyền Token nếu cần)
@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/init")
    void initStock(@RequestBody InitStockRequest request);

    @GetMapping("/api/v1/inventory/batch-stock")
    List<InventoryStockResponse> getBatchStock(@RequestParam("productIds") List<String> productIds);


}