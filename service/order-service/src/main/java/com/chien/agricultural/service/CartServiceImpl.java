package com.chien.agricultural.service;

import com.chien.agricultural.client.ProductClient;
import com.chien.agricultural.dto.response.CartItemResponse;
import com.chien.agricultural.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    private String getCartKey(String buyerId) {
        return "cart:" + buyerId;
    }

    // 1. Thêm vào giỏ (Atomic Increment)
    @Override
    public void addToCart(String buyerId, String productId, Integer quantity) {
        if (quantity <= 0) return;
        redisTemplate.opsForHash().increment(getCartKey(buyerId), productId, quantity);
    }

    // 2. Xóa khỏi giỏ
    @Override
    public void removeFromCart(String buyerId, String productId) {
        redisTemplate.opsForHash().delete(getCartKey(buyerId), productId);
    }

    // 3. Update (Ghi đè)
    @Override
    public void updateQuantity(String buyerId, String productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(buyerId, productId);
        } else {
            redisTemplate.opsForHash().put(getCartKey(buyerId), productId, quantity);
        }
    }

    // 4. Lấy giỏ hàng (Logic Merge)
    @Override
    public List<CartItemResponse> getCart(String buyerId) {
        // A. Lấy raw data
        Map<Object, Object> rawCart = redisTemplate.opsForHash().entries(getCartKey(buyerId));
        if (rawCart.isEmpty()) return new ArrayList<>();

        // B. Lấy danh sách Product ID
        Set<String> productIds = rawCart.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        // C. Gọi Feign Client (Batch Call)
        List<ProductResponse> products;
        try {
            // Cần try-catch để nếu Product Service chết thì không sập luôn tính năng xem giỏ (có thể trả về list rỗng hoặc lỗi tùy business)
            products = productClient.getProductsByIds(new ArrayList<>(productIds));
        } catch (Exception e) {
            log.error("Lỗi khi gọi Product Service để lấy thông tin sản phẩm trong giỏ hàng", e.getCause());
            return new ArrayList<>(); // Hoặc throw CustomException
        }

        // Map để tra cứu nhanh: Map<ProductId, ProductResponse>
        Map<String, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        // D. Ghép dữ liệu
        List<CartItemResponse> cartItems = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : rawCart.entrySet()) {
            String prodId = entry.getKey().toString();
            Integer qty = safeParseInt(entry.getValue()); // Dùng hàm parse an toàn

            ProductResponse product = productMap.get(prodId);

            if (product != null) {
                CartItemResponse item = CartItemResponse.builder()
                        .productId(prodId)
                        .productName(product.getName())
                        .productImage(product.getThumbnail())
                        .price(product.getPrice())
                        .availableQuantity(product.getAvailableQuantity()) // Thêm cái này để Frontend check tồn kho
                        .sellerId(product.getSellerProfileResponse().getSellerId())
                        .sellerName(product.getSellerProfileResponse().getFullName())
                        .quantity(qty)
                        .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(qty)))
                        .build();

                cartItems.add(item);
            } else {
                // Sản phẩm không tồn tại trong DB -> Xóa khỏi Redis để lần sau không load nữa
                removeFromCart(buyerId, prodId);
            }
        }

        // Sắp xếp lại theo thời gian thêm (hoặc theo tên) để list không bị nhảy lung tung
        // Vì Redis Hash không đảm bảo thứ tự
         cartItems.sort(Comparator.comparing(CartItemResponse::getProductName));

        return cartItems;
    }

    @Override
    public void clearCart(String buyerId, List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) return;
        // Xóa batch hiệu quả hơn loop
        redisTemplate.opsForHash().delete(getCartKey(buyerId), productIds.toArray());
    }

    // Helper method để parse số an toàn từ Redis
    private Integer safeParseInt(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}