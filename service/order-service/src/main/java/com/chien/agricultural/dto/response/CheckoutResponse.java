package com.chien.agricultural.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {

    // 1. KHỐI ĐỊA CHỈ NHẬN HÀNG (Lấy từ User Service)
    // Để frontend hiển thị lại: "Giao tới: Chiến - 094... - Bắc Từ Liêm..."
    private AddressResponse shippingAddress;

    // 2. KHỐI ĐƠN HÀNG (Đã tách theo Shop)
    // List này sẽ được map ra giao diện bên trái
    private List<ShopOrderGroup> orders;

    // 3. KHỐI TỔNG TIỀN (Hiển thị bên phải/Bottom)
    private BigDecimal totalProductPrice; // Tổng tiền hàng (chưa ship)
    private BigDecimal totalShippingFee;  // Tổng tiền ship của tất cả các shop
    private BigDecimal finalAmount;       // Số tiền khách phải trả (Product + Ship)

    /* ==============================================
       INNER CLASS: ĐẠI DIỆN CHO 1 ĐƠN HÀNG CỦA 1 SHOP
       ============================================== */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShopOrderGroup {
        private String sellerId;
        private String sellerName; // Tên Shop/Nông trại (Hiển thị trên Header của Card)
        private String sellerAvatar; // (Optional) Logo shop

        // List sản phẩm khách mua của Shop này
        private List<CheckoutItemResponse> items;

        // Các con số tính toán riêng cho Shop này
        private Double distance; // Khoảng cách từ Shop -> Khách (km) (Optional: để hiển thị cho uy tín)
        private BigDecimal shippingFee; // Phí ship tính riêng cho Shop này
        private BigDecimal shopTotal;   // Tổng tiền đơn này (Items + Ship)
    }

    /* ==============================================
       INNER CLASS: CHI TIẾT SẢN PHẨM
       ============================================== */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckoutItemResponse {
        private String productId;
        private String productName;
        private String productImage; // Thumbnail

        private Integer quantity;    // Số lượng mua
        private BigDecimal price;    // Đơn giá tại thời điểm checkout
        private BigDecimal totalPrice; // price * quantity
    }
}