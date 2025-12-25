package com.chien.agricultural.service;

import com.chien.agricultural.client.InventoryClient;
import com.chien.agricultural.client.ProductClient;
import com.chien.agricultural.client.UserClient;
import com.chien.agricultural.dto.request.CreateOrderRequest;
import com.chien.agricultural.dto.request.DeductStockRequest;
import com.chien.agricultural.dto.response.AddressResponse;
import com.chien.agricultural.dto.response.OrderResponse;
import com.chien.agricultural.dto.response.ProductResponse;
import com.chien.agricultural.dto.response.SellerProfileResponse;
import com.chien.agricultural.entity.Order;
import com.chien.agricultural.entity.OrderItem;
import com.chien.agricultural.entity.OrderStatus;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.mapper.OrderMapper;
import com.chien.agricultural.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CartService cartService;
    private final UserClient userClient;
    private final ShippingService shippingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> createOrder(CreateOrderRequest request, String buyerId) {
        List<String> createdOrderIds = new ArrayList<>();

        // 1. Lấy Address
        AddressResponse address = userClient.getAddressById(request.getAddressId());
        if (address == null) throw new AppException("Địa chỉ không tồn tại", HttpStatus.BAD_REQUEST);

        // 2. Batch Get Products
        List<String> productIds = request.getItems().stream()
                .map(CreateOrderRequest.OrderItemRequest::getProductId)
                .distinct()
                .toList();

        List<ProductResponse> products = productClient.getProductsByIds(productIds);
        Map<String, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        // 3. Group by Seller
        Map<String, List<CreateOrderRequest.OrderItemRequest>> itemsBySeller = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> {
                    ProductResponse p = productMap.get(item.getProductId());
                    if (p == null) throw new AppException("SP ID " + item.getProductId() + " không tồn tại", HttpStatus.BAD_REQUEST);
                    // Cần check null seller profile để tránh lỗi
                    if (p.getSellerProfileResponse() == null) throw new AppException("Sản phẩm chưa có thông tin người bán", HttpStatus.INTERNAL_SERVER_ERROR);
                    return p.getSellerProfileResponse().getSellerId();
                }));

        // 4. Duyệt từng Shop
        for (var entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<CreateOrderRequest.OrderItemRequest> shopItemsReq = entry.getValue();

            ProductResponse firstProduct = productMap.get(shopItemsReq.getFirst().getProductId());
            SellerProfileResponse sellerInfo = firstProduct.getSellerProfileResponse();

            // Init Order
            Order order = new Order();
            // ID: ORD + Timestamp + 4 ký tự random UUID
            String orderId = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
            order.setId(orderId);
            order.setBuyerId(buyerId);
            order.setSellerId(sellerId);

            // Snapshot Address
            String fullAddress = address.getDetailAddress() + ", " + address.getWardName() + ", " + address.getDistrictName() + ", " + address.getProvinceName();
            order.setRecipientName(address.getRecipientName());
            order.setRecipientPhone(address.getPhone());
            order.setShippingAddress(fullAddress);

            order.setPaymentMethod(request.getPaymentMethod());
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(Instant.now());

            BigDecimal shopTotalAmount = BigDecimal.ZERO;
            double shopTotalWeight = 0.0;
            List<OrderItem> orderItemsEntity = new ArrayList<>();
            List<DeductStockRequest> stockRequests = new ArrayList<>();

            // 5. Loop Items (ĐÃ SỬA: BỎ LOGIC VARIANT - ĐỒNG BỘ VỚI PREVIEW)
            for (var itemReq : shopItemsReq) {
                ProductResponse p = productMap.get(itemReq.getProductId());

                BigDecimal unitPrice = p.getPrice();
                double unitWeight = p.getWeight() != null ? p.getWeight() : 0.5;
                String unit = p.getUnit();

                // Tạo OrderItem
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productId(p.getId())
                        .productName(p.getName())
                        .productImage(p.getThumbnail())
                        .price(unitPrice)
                        .quantity(itemReq.getQuantity())
                        .unit(unit)
                        .build();

                orderItemsEntity.add(orderItem);

                shopTotalAmount = shopTotalAmount.add(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                shopTotalWeight += (unitWeight * itemReq.getQuantity());

                stockRequests.add(new DeductStockRequest(p.getId(), itemReq.getQuantity()));
            }

            // 6. Trừ kho (Deduct Batch)
            try {
                inventoryClient.deductStockBatch(stockRequests);
            } catch (FeignException e) {
                // Log lỗi chi tiết để debug
                log.error("Lỗi trừ kho: status={}, body={}", e.status(), e.contentUTF8());
                throw new AppException("Sản phẩm đã hết hàng hoặc lỗi hệ thống kho.", HttpStatus.BAD_REQUEST);
            }

            // 7. Tính ship (Logic phải giống hệt Preview)
            BigDecimal shippingFee = BigDecimal.valueOf(30000); // Default fallback
            if (sellerInfo.getLatitude() != null && sellerInfo.getLongitude() != null
                    && address.getLatitude() != null && address.getLongitude() != null) {
                shippingFee = shippingService.calculateFee(
                        address.getLatitude(), address.getLongitude(),
                        sellerInfo.getLatitude(), sellerInfo.getLongitude(),
                        shopTotalWeight
                );
            }

            order.setTotalAmount(shopTotalAmount);
            order.setShippingFee(shippingFee);
            order.setFinalAmount(shopTotalAmount.add(shippingFee));
            order.setItems(orderItemsEntity);

            // Lưu đơn
            orderRepository.save(order);
            createdOrderIds.add(orderId);
        }

        // 8. Clear Cart
        try {
            cartService.clearCart(buyerId, productIds);
        } catch (Exception e) {
            log.warn("Lỗi xóa giỏ hàng: " + e.getMessage());
        }

        return createdOrderIds;
    }


    // Lấy danh sách đơn hàng của Seller
    @Override
    public List<OrderResponse> getOrdersBySeller(String sellerId) {
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(OrderMapper::toOrderResponse)
                .toList();
    }

    // Cập nhật trạng thái đơn hàng (Duyệt / Hủy / Giao)
    @Override
    public void updateOrderStatus(String sellerId, String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Security check: Đảm bảo Seller này là chủ của đơn hàng
        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Bạn không có quyền xử lý đơn hàng này");
        }

        // Validate logic chuyển trạng thái
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Không thể thay đổi trạng thái đơn hàng đã kết thúc");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            // 1. Chỉ hoàn kho nếu đơn hàng trước đó KHÔNG PHẢI là Cancelled (tránh hoàn 2 lần)
            // Và trạng thái cũ chưa phải là 'đã giao thành công' (tùy nghiệp vụ)

            // 2. Map từ OrderItem -> StockUpdateDto
            List<DeductStockRequest> itemsToRestore = order.getItems().stream()
                    .map(item -> new DeductStockRequest(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList());

            // 3. Gọi Inventory Service
            try {
                inventoryClient.restoreStock(itemsToRestore);
            } catch (Exception e) {
                // Quan trọng: Nếu hoàn kho lỗi -> Không cho phép hủy đơn (hoặc log lại để xử lý sau)
                throw new RuntimeException("Lỗi hệ thống: Không thể hoàn kho sản phẩm. Vui lòng thử lại.");
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getOrderByBuyer(String userId) {

        return orderRepository.findByBuyerId(userId)
                .stream()
                .map(OrderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public void cancelOrder(String userId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Security check: Đảm bảo User này là chủ của đơn hàng
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        // Validate logic chuyển trạng thái
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.SHIPPING) {
            throw new RuntimeException("Không thể hủy đơn hàng");
        }

        // 1. Map từ OrderItem -> StockUpdateDto
        List<DeductStockRequest> itemsToRestore = order.getItems().stream()
                .map(item -> new DeductStockRequest(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        // 2. Gọi Inventory Service
        try {
            inventoryClient.restoreStock(itemsToRestore);
        } catch (Exception e) {
            // Quan trọng: Nếu hoàn kho lỗi -> Không cho phép hủy đơn (hoặc log lại để xử lý sau)
            throw new RuntimeException("Lỗi hệ thống: Không thể hoàn kho sản phẩm. Vui lòng thử lại.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}