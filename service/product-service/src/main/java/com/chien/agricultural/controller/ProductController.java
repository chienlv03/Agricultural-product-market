package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.CreateProductRequest;
import com.chien.agricultural.dto.request.UpdateProductRequest;
import com.chien.agricultural.model.Product;
import com.chien.agricultural.model.ProductStatus;
import com.chien.agricultural.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1. API Tạo sản phẩm (Chỉ Seller mới được gọi)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Quan trọng: Đổi content type
    public ResponseEntity<Product> createProduct(
            @RequestPart("data") CreateProductRequest request, // JSON thông tin
            @RequestPart(value = "images", required = false) List<MultipartFile> images, // Ảnh sản phẩm
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos  // Video quay vườn
    ) {
        return ResponseEntity.ok(productService.createProduct(request, images, videos));
    }

    // 3. API Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductDetail(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // 4. API Xem chi tiết (Theo Slug - SEO)
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    // 5. API cho Nông dân xem hàng của mình (Quản lý)
    @GetMapping("/seller/me")
    public ResponseEntity<Page<Product>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getMyProducts(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("active")
    public ResponseEntity<Page<Product>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(productService.getActiveProducts(pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<Product>> getPendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(productService.getPendingProducts(pageable));
    }

    // 2. Duyệt hoặc Từ chối sản phẩm
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestParam ProductStatus status) {
        productService.updateProductStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestPart("data") UpdateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request, newImages));
    }
}