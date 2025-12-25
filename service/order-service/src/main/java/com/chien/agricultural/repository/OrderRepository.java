package com.chien.agricultural.repository;

import com.chien.agricultural.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    // Tìm đơn hàng của Seller, sắp xếp mới nhất lên đầu
    List<Order> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    List<Order> findByBuyerId(String userId);
}
