package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.CategoryRequest;
import com.chien.agricultural.model.Category;
import com.chien.agricultural.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // --- SỬA API CREATE ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> create(
            @RequestPart("data") @Valid CategoryRequest request, // JSON thông tin
            @RequestPart(value = "image", required = false) MultipartFile image // File ảnh
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        CategoryRequest categoryRequest = objectMapper.convertValue(request, CategoryRequest.class);
        return ResponseEntity.ok(categoryService.createCategory(categoryRequest, image));
    }

    // --- SỬA API UPDATE ---
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> update(
            @PathVariable String id,
            @RequestPart("data") @Valid CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request, image));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Đã xóa danh mục thành công");
    }
}