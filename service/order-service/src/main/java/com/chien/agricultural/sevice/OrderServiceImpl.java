package com.chien.agricultural.sevice;

import com.chien.agricultural.client.InventoryClient;
import com.chien.agricultural.client.ProductClient;
import com.chien.agricultural.dto.request.CreateOrderRequest;
import com.chien.agricultural.dto.request.DeductStockRequest;
import com.chien.agricultural.dto.request.OrderItemRequest;
import com.chien.agricultural.dto.response.ProductResponse;
import com.chien.agricultural.entity.Order;
import com.chien.agricultural.entity.OrderItem;
import com.chien.agricultural.entity.OrderStatus;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.repository.OrderRepository;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    public List<String> createOrder(CreateOrderRequest request, String userId) {
        List<String> createdOrderIds = new ArrayList<>();

        // 1. Lấy thông tin chi tiết sản phẩm và Gom nhóm theo Seller
        // Map<SellerId, List<CartItem>>
        Map<String, List<OrderItemDetail>> itemsBySeller = new HashMap<>();

        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductResponse product;
            // Gọi Product Service lấy thông tin (Giá, Seller)
            // TODO: Tối ưu bằng cách gọi API getListByIds thay vì for loop gọi từng cái
            try {
                product = productClient.getProductById(itemRequest.getProductId());
            } catch (FeignException.NotFound e) {
                throw new AppException("Sản phẩm ID " + itemRequest.getProductId() + " không tồn tại hoặc đã bị xóa.", HttpStatus.BAD_REQUEST);
            }

            String sellerId = product.getSeller().getId();

            itemsBySeller.computeIfAbsent(sellerId, k -> new ArrayList<>())
                    .add(new OrderItemDetail(product, itemRequest.getQuantity()));
        }

        // 2. Duyệt qua từng Seller để tạo đơn riêng
        for (Map.Entry<String, List<OrderItemDetail>> entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<OrderItemDetail> items = entry.getValue();

            // --- TẠO ĐƠN HÀNG ---
            Order order = new Order();
            String orderId = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
            order.setId(orderId);
            order.setUserId(userId);
            order.setSellerId(sellerId);
            order.setRecipientName(request.getRecipientName());
            order.setRecipientPhone(request.getRecipientPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(Instant.now());

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            // Xử lý từng sản phẩm trong đơn này
            for (OrderItemDetail itemDetail : items) {
                ProductResponse p = itemDetail.product;
                Integer qty = itemDetail.quantity;

                // A. Gọi Inventory trừ kho (Nếu hết hàng nó sẽ throw Exception -> Rollback toàn bộ)
                try {
                    inventoryClient.deductStock(new DeductStockRequest(p.getId(), qty));
                } catch (FeignException e) {
                    // Nếu bên Inventory báo lỗi (400 hoặc 500)
                    if (e.status() == 400) {
                        throw new AppException("Sản phẩm " + p.getName() + " đã hết hàng hoặc không đủ số lượng.", HttpStatus.BAD_REQUEST);
                    }
                    throw new AppException("Lỗi hệ thống kho: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // B. Tạo Order Item
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productId(p.getId())
                        .productName(p.getName())
                        .productImage(p.getThumbnail())
                        .price(p.getPrice()) // Giá lấy từ DB, không lấy từ Frontend
                        .quantity(qty)
                        .build();

                orderItems.add(orderItem);

                // Cộng tiền
                totalAmount = totalAmount.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
            }

            // Tính phí ship (Demo: fix cứng hoặc tính sau)
            BigDecimal shippingFee = BigDecimal.valueOf(30000); // Giả sử 30k

            order.setTotalAmount(totalAmount);
            order.setShippingFee(shippingFee);
            order.setFinalAmount(totalAmount.add(shippingFee));
            order.setItems(orderItems);

            // Lưu đơn
            orderRepository.save(order);
            createdOrderIds.add(orderId);
        }

        return createdOrderIds;
    }

    // Class phụ để giữ data trong lúc xử lý
    @AllArgsConstructor
    private static class OrderItemDetail {
        ProductResponse product;
        Integer quantity;
    }
}