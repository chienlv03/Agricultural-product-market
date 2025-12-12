package com.chien.agricultural.sevice;

import com.chien.agricultural.dto.request.CreateOrderRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {
    List<String> createOrder(CreateOrderRequest request, String userId);
}
