package com.chien.agricultural.service;

import com.chien.agricultural.dto.response.CartItemResponse;

import java.util.List;

public interface CartService {
    // 1. Thêm vào giỏ
    void addToCart(String buyerId, String productId, Integer quantity);

    // 2. Xóa khỏi giỏ
    void removeFromCart(String buyerId, String productId);

    // 3. Cập nhật số lượng (Ghi đè)
    void updateQuantity(String buyerId, String productId, Integer quantity);

    // 4. Lấy giỏ hàng chi tiết (Khó nhất)
    List<CartItemResponse> getCart(String buyerId);

    // 5. Clear giỏ hàng (Sau khi checkout)
    void clearCart(String buyerId, List<String> productIds);
}
