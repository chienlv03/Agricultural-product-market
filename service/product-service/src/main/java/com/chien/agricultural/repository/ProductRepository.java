package com.chien.agricultural.repository;

import com.chien.agricultural.model.Product;
import com.chien.agricultural.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {

    // Lấy sản phẩm theo Slug (cho SEO URL)
    Optional<Product> findBySlug(String slug);

    // Lấy danh sách sản phẩm hiển thị trang chủ (có phân trang)
    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    // Tìm các sản phẩm của một nông dân cụ thể
    Page<Product> findAllBySeller_Id(String sellerId, Pageable pageable);
}
