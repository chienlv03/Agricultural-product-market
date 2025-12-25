package com.chien.agricultural.service;

import com.chien.agricultural.client.ProductClient;
import com.chien.agricultural.client.UserClient;
import com.chien.agricultural.dto.request.CheckoutItem;
import com.chien.agricultural.dto.request.CheckoutPreviewRequest;
import com.chien.agricultural.dto.response.*;
import com.chien.agricultural.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckoutServiceImpl implements CheckoutService {

    private final ProductClient productClient;
    private final UserClient userClient;
    private final ShippingService shippingService;

    @Override
    public CheckoutResponse previewOrder(CheckoutPreviewRequest request) {
        // 1. Lấy thông tin người mua
        AddressResponse buyerAddress = userClient.getAddressById(request.getAddressId());
        if (buyerAddress == null) throw new AppException("Địa chỉ nhận hàng không tồn tại", HttpStatus.BAD_REQUEST);

        // 2. Batch Get Products
        List<String> productIds = request.getItems().stream()
                .map(CheckoutItem::getProductId)
                .distinct()
                .toList();

        List<ProductResponse> products = productClient.getProductsByIds(productIds);
        Map<String, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        // 3. Group by Seller
        Map<String, List<CheckoutItem>> itemsBySeller = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> {
                    ProductResponse p = productMap.get(item.getProductId());
                    // Nếu sản phẩm lỗi hoặc không có seller -> Gom vào nhóm UNKNOWN để lọc bỏ
                    return (p != null && p.getSellerProfileResponse() != null)
                            ? p.getSellerProfileResponse().getSellerId()
                            : "UNKNOWN";
                }));

        // 4. Xử lý từng nhóm Seller
        List<CheckoutResponse.ShopOrderGroup> shopOrderGroups = new ArrayList<>();
        BigDecimal totalProductPrice = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        for (var entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            if ("UNKNOWN".equals(sellerId)) continue;

            List<CheckoutItem> cartItems = entry.getValue();

            // Lấy thông tin Seller
            ProductResponse firstProduct = productMap.get(cartItems.getFirst().getProductId());
            SellerProfileResponse sellerInfo = firstProduct.getSellerProfileResponse();

            BigDecimal shopProductTotal = BigDecimal.ZERO;
            double shopTotalWeight = 0.0;
            List<CheckoutResponse.CheckoutItemResponse> itemResponses = new ArrayList<>();

            for (var itemReq : cartItems) {
                ProductResponse p = productMap.get(itemReq.getProductId());
                if (p == null) continue;

                // --- ĐỒNG BỘ LOGIC: Lấy giá gốc, bỏ qua Variant ---
                BigDecimal unitPrice = p.getPrice();
                double unitWeight = p.getWeight() != null ? p.getWeight() : 0.5; // Mặc định 0.5kg
                String unit = p.getUnit(); // Đơn vị tính

                // Tính tiền
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                shopProductTotal = shopProductTotal.add(lineTotal);
                shopTotalWeight += (unitWeight * itemReq.getQuantity());

                itemResponses.add(CheckoutResponse.CheckoutItemResponse.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .productImage(p.getThumbnail())
                        .quantity(itemReq.getQuantity())
                        .price(unitPrice)
                        .totalPrice(lineTotal)
                        .build());
            }

            // 5. Tính phí ship
            BigDecimal shippingFee;
            // Chỉ tính nếu có tọa độ đầy đủ
            if (sellerInfo.getLatitude() != null && sellerInfo.getLongitude() != null
                    && buyerAddress.getLatitude() != null && buyerAddress.getLongitude() != null) {

                shippingFee = shippingService.calculateFee(
                        buyerAddress.getLatitude(), buyerAddress.getLongitude(),
                        sellerInfo.getLatitude(), sellerInfo.getLongitude(),
                        shopTotalWeight
                );
            } else {
                // Fallback: Nếu thiếu tọa độ thì lấy phí mặc định 30k (hoặc ném lỗi tùy nghiệp vụ)
                shippingFee = BigDecimal.valueOf(30000);
            }

            totalProductPrice = totalProductPrice.add(shopProductTotal);
            totalShippingFee = totalShippingFee.add(shippingFee);

            shopOrderGroups.add(CheckoutResponse.ShopOrderGroup.builder()
                    .sellerId(sellerId)
                    .sellerName(sellerInfo.getFarmName())
                    .items(itemResponses)
                    .distance(0.0) // Có thể set distance trả về từ shippingService nếu cần hiển thị
                    .shippingFee(shippingFee)
                    .shopTotal(shopProductTotal.add(shippingFee))
                    .build());
        }

        return CheckoutResponse.builder()
                .shippingAddress(buyerAddress)
                .orders(shopOrderGroups)
                .totalProductPrice(totalProductPrice)
                .totalShippingFee(totalShippingFee)
                .finalAmount(totalProductPrice.add(totalShippingFee))
                .build();
    }
}
