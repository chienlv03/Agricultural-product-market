package com.chien.agricultural.client;

import com.chien.agricultural.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {
    // Chúng ta cần viết thêm API internal bên Product Service để lấy chi tiết gọn nhẹ
    // Hoặc dùng tạm API getDetail public
    @GetMapping("/api/v1/products/{id}")
    ProductResponse getProductById(@PathVariable String id);

    @GetMapping("api/v1/products/by-ids")
    List<ProductResponse> getProductsByIds(@RequestParam List<String> ids);
}