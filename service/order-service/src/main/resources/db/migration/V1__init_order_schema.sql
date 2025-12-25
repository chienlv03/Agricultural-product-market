CREATE SCHEMA IF NOT EXISTS order_service;

-- 1. Bảng Đơn Hàng
CREATE TABLE order_service.orders
(
    id               VARCHAR(50) PRIMARY KEY,

    buyer_id         VARCHAR(50)    NOT NULL,
    seller_id        VARCHAR(50)    NOT NULL,

    total_amount     DECIMAL(15, 2) NOT NULL,      -- Tổng tiền hàng
    shipping_fee     DECIMAL(15, 2) DEFAULT 0,     -- Phí ship
    final_amount     DECIMAL(15, 2) NOT NULL,      -- Khách phải trả

    recipient_name   VARCHAR(100)   NOT NULL,
    recipient_phone  VARCHAR(20)    NOT NULL,
    shipping_address TEXT           NOT NULL,

    -- Trạng thái & Loại đơn
    status           VARCHAR(20)    NOT NULL,      -- PENDING, CONFIRMED, SHIPPING...
    payment_method   VARCHAR(20)    NOT NULL,      -- COD, VNPAY
    is_pre_order     BOOLEAN        DEFAULT FALSE, -- Đánh dấu đơn đặt trước

    created_at       TIMESTAMP WITH TIME ZONE,
    updated_at       TIMESTAMP WITH TIME ZONE
);

-- 2. Bảng Chi Tiết Đơn Hàng
CREATE TABLE order_service.order_items
(
    id            UUID PRIMARY KEY,        -- ID tự sinh
    order_id      VARCHAR(50)    NOT NULL REFERENCES order_service.orders (id),

    product_id    VARCHAR(50)    NOT NULL,
    product_name  VARCHAR(255)   NOT NULL, -- Lưu tên lúc mua (phòng khi shop đổi tên)
    product_image VARCHAR(500),

    quantity      INTEGER        NOT NULL,
    price         DECIMAL(15, 2) NOT NULL, -- Giá tại thời điểm mua
    unit          VARCHAR(20)    NOT NULL  -- Đơn vị tính
);

CREATE INDEX idx_orders_user_id ON order_service.orders (buyer_id);
CREATE INDEX idx_orders_seller_id ON order_service.orders (seller_id);