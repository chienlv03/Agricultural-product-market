package com.chien.agricultural.repository;

import com.chien.agricultural.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
