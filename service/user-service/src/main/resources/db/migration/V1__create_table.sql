CREATE EXTENSION IF NOT EXISTS postgis SCHEMA public;

-- 1. Bảng users
create table user_service.users
(
    -- Gợi ý: Dùng chính ID của Keycloak làm PK để dễ truy xuất
    id         varchar(36) primary key,
    phone      varchar(15) unique not null,
    full_name  varchar(100),
    email      varchar(100) unique, -- Nên thêm email để gửi hóa đơn
    avatar_url text,

    -- Enum role & status
    role       varchar(20) check (role in ('BUYER', 'SELLER', 'ADMIN'))                default 'BUYER',
    status     varchar(20) check (status in ('ACTIVE', 'PENDING', 'BANNED', 'LOCKED')) default 'ACTIVE',

    -- Audit log
    created_at timestamp                                                               default CURRENT_TIMESTAMP,
    updated_at timestamp                                                               default CURRENT_TIMESTAMP
);

-- 2. Bảng seller_profiles
create table user_service.seller_profiles
(
    user_id          varchar(36) primary key references user_service.users (id),

    -- KYC (Định danh)
    id_card_front    text,
    id_card_back     text,
    tax_code         varchar(50), -- Mã số thuế (nếu có, để xuất hóa đơn)

    -- Thông tin vườn
    farm_name        varchar(200) not null,
    farm_description text,        -- Mô tả câu chuyện nông sản
    farm_photos      text[],      -- Array URL
    farm_address     text,

    -- Tọa độ vườn (Quan trọng để tính ship từ vườn -> khách)
    location         public.geography(Point, 4326),

    -- Chứng nhận (Dùng JSONB để lưu nhiều chứng nhận)
    -- Cấu trúc: [{"type": "VietGAP", "url": "...", "expired_at": "..."}, {"type": "OCOP", "url": "..."}]
    certifications   jsonb,

    -- Trạng thái duyệt
    verified         boolean default false,
    verified_at      timestamp,
    verified_by      varchar(36), -- Người duyệt (Admin ID)
    rejection_reason text         -- Lý do từ chối (nếu có)
);

-- Index cho tìm kiếm PostGIS seller
CREATE INDEX idx_seller_location ON user_service.seller_profiles USING GIST (location);

-- 3. Bảng addresses
create table user_service.addresses
(
    id             uuid primary key,
    user_id        varchar(36) references user_service.users (id),
    recipient_name varchar(100),
    phone          varchar(15),

    -- Địa chỉ chi tiết (số nhà, ngõ...)
    detail_address text,

    -- Lưu cả Tên và Mã (Code để gọi API GHN/GHTK)
    province_id    integer,     -- Ví dụ: 201 (Hà Nội)
    province_name  varchar(50),
    district_id    integer,     -- Ví dụ: 1482 (Quận Cầu Giấy)
    district_name  varchar(50),
    ward_code      varchar(20), -- Ví dụ: "1A0807"
    ward_name      varchar(50),

    -- Tọa độ người mua (Để gợi ý "Nông sản gần bạn")
    location       public.geography(Point, 4326),

    is_default     boolean   default false,
    created_at     timestamp default CURRENT_TIMESTAMP
);

-- Index cho tìm kiếm PostGIS buyer (optional, dùng cho analytics)
CREATE INDEX idx_address_location ON user_service.addresses USING GIST (location);