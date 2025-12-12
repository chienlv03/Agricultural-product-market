package com.chien.agricultural.sevice;

import com.chien.agricultural.client.ProductClient;
import com.chien.agricultural.dto.response.CartItemResponse;
import com.chien.agricultural.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    private String getCartKey(String userId) {
        return "cart:" + userId;
    }

    // 1. Thêm vào giỏ
    @Override
    public void addToCart(String userId, String productId, Integer quantity) {
        // Kiểm tra quantity > 0
        if (quantity <= 0) return;

        // HINCRBY: Tự động cộng dồn số lượng. Nếu chưa có thì tạo mới.
        // Key chính: cart:{userId}, HashKey: productId, Value: quantity (dạng integer)
        redisTemplate.opsForHash().increment(getCartKey(userId), productId, quantity);
    }

    // 2. Xóa khỏi giỏ
    @Override
    public void removeFromCart(String userId, String productId) {
        redisTemplate.opsForHash().delete(getCartKey(userId), productId);
    }

    // 3. Cập nhật số lượng (Ghi đè)
    @Override
    public void updateQuantity(String userId, String productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, productId);
        } else {
            redisTemplate.opsForHash().put(getCartKey(userId), productId, quantity);
        }
    }

    // 4. Lấy giỏ hàng chi tiết (Khó nhất)
    @Override
    public List<CartItemResponse> getCart(String userId) {
        // A. Lấy raw data từ Redis: Map<ProductId, Quantity>
        Map<Object, Object> rawCart = redisTemplate.opsForHash().entries(getCartKey(userId));

        if (rawCart.isEmpty()) return new ArrayList<>();

        // B. Lấy danh sách ID sản phẩm
        // Convert Object key sang String vì Redis lưu dạng binary/string
        Set<String> productIds = rawCart.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        // C. Gọi Product Service lấy thông tin chi tiết (Bulk API)
        // Bạn cần viết thêm API getByIds bên Product Service nhé!
        List<ProductResponse> products = productClient.getProductsByIds(new ArrayList<>(productIds));

        // Convert List thành Map để dễ tra cứu: Map<ProductId, ProductResponse>
        Map<String, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        // D. Ghép dữ liệu (Merge)
        List<CartItemResponse> cartItems = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : rawCart.entrySet()) {
            String prodId = entry.getKey().toString();
            int qty = Integer.parseInt(entry.getValue().toString());

            ProductResponse product = productMap.get(prodId);

            if (product != null) {
                // Sản phẩm còn tồn tại
                CartItemResponse item = CartItemResponse.builder()
                        .productId(prodId)
                        .productName(product.getName())
                        .productImage(product.getThumbnail())
                        .price(product.getPrice())
                        .sellerId(product.getSeller().getId())
                        .sellerName(product.getSeller().getName())
                        .quantity(qty)
                        .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(qty)))
                        .build();

                cartItems.add(item);
            } else {
                // Sản phẩm đã bị xóa khỏi DB -> Tự động xóa khỏi giỏ Redis
                removeFromCart(userId, prodId);
            }
        }

        return cartItems;
    }

    // 5. Clear giỏ hàng (Sau khi checkout)
    @Override
    public void clearCart(String userId, List<String> productIds) {
        // Xóa các sản phẩm đã mua
        for (String pid : productIds) {
            redisTemplate.opsForHash().delete(getCartKey(userId), pid);
        }
    }
}