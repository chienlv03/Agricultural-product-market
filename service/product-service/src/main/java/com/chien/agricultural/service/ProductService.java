package com.chien.agricultural.service;

import com.chien.agricultural.client.InventoryClient;
import com.chien.agricultural.client.UserClient;
import com.chien.agricultural.dto.request.CreateProductRequest;
import com.chien.agricultural.dto.request.InitStockRequest;
import com.chien.agricultural.dto.request.UpdateProductRequest;
import com.chien.agricultural.dto.response.InventoryStockResponse;
import com.chien.agricultural.dto.response.ProductResponse;
import com.chien.agricultural.dto.response.UserResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.mapper.ProductMapper;
import com.chien.agricultural.model.Product;
import com.chien.agricultural.model.ProductStatus;
import com.chien.agricultural.model.SellerInfo;
import com.chien.agricultural.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductService {

    private final ProductRepository productRepository;
    private final UserClient userClient; // Feign Client gọi User Service
    private final MinioService minioService;
    private final InventoryClient inventoryClient;

    @Transactional
    public Product createProduct(CreateProductRequest request,
                                 List<MultipartFile> images,
                                 List<MultipartFile> videos) {

        // 1. Lấy thông tin người bán (Seller)
        SellerInfo sellerInfo = userClient.getSellerById();

        // 2. Xử lý Upload Ảnh (Images)
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            imageUrls = images.stream()
                    .map(minioService::uploadFile) // Upload từng ảnh
                    .toList();
        }

        // 3. Xử lý Upload Video (Videos)
        List<String> videoUrls = new ArrayList<>();
        if (videos != null && !videos.isEmpty()) {
            videoUrls = videos.stream()
                    .map(minioService::uploadFile)
                    .toList();
        }

        // 4. Map dữ liệu sang Entity
        Product product = Product.builder()
                .sku(generateSku(request.getName()))
                .name(request.getName())
                .slug(toSlug(request.getName()))
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit())

                // Lưu URL ảnh/video vừa upload vào đây
                .images(imageUrls)
                .videos(videoUrls)
                .thumbnail(imageUrls.isEmpty() ? null : imageUrls.getFirst()) // Lấy ảnh đầu tiên làm thumbnail

                .categoryIds(request.getCategoryIds())
                .attributes(request.getAttributes())
                .status(ProductStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        product.setSeller(sellerInfo);

        Product savedProduct = productRepository.save(product);

        try {
            InitStockRequest stockRequest = InitStockRequest.builder()
                    .productId(savedProduct.getId())
                    .quantity(request.getQuantity()) // Lấy số lượng từ request
                    .harvestDate(request.getAttributes().getHarvestDate()) // Lấy ngày thu hoạch
                    .build();

            inventoryClient.initStock(stockRequest);

        } catch (Exception e) {
            productRepository.deleteById(savedProduct.getId());
            throw new AppException("Lỗi khởi tạo kho hàng: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return savedProduct;
    }

    // Tiện ích tạo Slug: "Cà chua bi" -> "ca-chua-bi"
    private String toSlug(String input) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH) + "-" + System.currentTimeMillis();
    }

    // Tiện ích tạo SKU: "SP-RANDOM"
    private String generateSku(String name) {
        return "AGRI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 2. Lấy danh sách sản phẩm (Có phân trang & Sắp xếp)
    public Page<Product> findAllByStatus(ProductStatus productStatus, Pageable pageable) {
        Page<Product> product = productRepository.findAllByStatus(productStatus, pageable);
        // Assuming `getStock` is updated to accept a List<String>
        List<String> productIds = product.getContent().stream()
                .map(Product::getId)
                .toList();

        List<InventoryStockResponse> stockList = new ArrayList<>();
        try {
            if (!productIds.isEmpty()) {
                stockList = inventoryClient.getBatchStock(productIds);
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi Inventory Service", e);
            // Nếu lỗi thì coi như stockList rỗng, mặc định quantity = 0
        }

        Map<String, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(
                        InventoryStockResponse::getProductId,
                        InventoryStockResponse::getAvailableQuantity,
                        (existing, replacement) -> existing // Nếu trùng key thì giữ cái cũ
                ));

        return product.map(p -> {
            p.setAvailableQuantity(stockMap.getOrDefault(p.getId(), 0));
            return p;
        });
    }

    // 3. Lấy chi tiết sản phẩm theo ID
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại hoặc đã bị xóa", HttpStatus.NOT_FOUND));
    }

    // 4. Lấy chi tiết theo Slug (Cho SEO Friendly URL)
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND));
    }

    // 5. Lấy sản phẩm của Shop mình (Seller quản lý)
    public Page<Product> getMyProducts(Pageable pageable) {
        // Lấy ID người dùng hiện tại từ Token
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return productRepository.findAllBySeller_SellerId(currentUserId, pageable);
    }

    // 1. Lấy sản phẩm cho Trang chủ (Mặc định chỉ lấy ACTIVE)
    public Page<Product> getActiveProducts(Pageable pageable) {
        return findAllByStatus(ProductStatus.ACTIVE, pageable);
    }

    // 2. Lấy sản phẩm chờ duyệt (Cho Admin)
    public Page<Product> getPendingProducts(Pageable pageable) {
        return findAllByStatus(ProductStatus.PENDING, pageable);
    }

    public void updateProductStatus(String id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND));
        product.setStatus(status);
        productRepository.save(product);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND));
        productRepository.delete(product);
    }

    // ... imports

    @Transactional
    public Product updateProduct(String id, UpdateProductRequest request, List<MultipartFile> newImages) {
        // 1. Tìm sản phẩm
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND));

        // 2. Kiểm tra quyền (Chỉ chủ sở hữu mới được sửa)
        String currentUserId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (!product.getSeller().getSellerId().equals(currentUserId)) {
            // (Mở rộng: Admin vẫn được quyền sửa)
            throw new AppException("Bạn không có quyền sửa sản phẩm này", HttpStatus.FORBIDDEN);
        }

        // 3. Cập nhật thông tin cơ bản (Nếu field nào null thì giữ nguyên hoặc cho phép null tùy logic)
        if (request.getName() != null) {
            product.setName(request.getName());
            // Lưu ý: Có thể update slug nếu muốn, hoặc giữ nguyên để SEO ổn định
        }
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getCategoryIds() != null) product.setCategoryIds(request.getCategoryIds());
        if (request.getAttributes() != null) {
            // Logic merge attributes phức tạp hơn chút, ở đây gán đè cho nhanh
            product.setAttributes(request.getAttributes());
        }

        // 4. Xử lý hình ảnh (Logic quan trọng)
        List<String> finalImages = new ArrayList<>();

        // 4a. Giữ lại ảnh cũ (theo danh sách client gửi lên)
        if (request.getKeepImages() != null && !request.getKeepImages().isEmpty()) {
            finalImages.addAll(request.getKeepImages());
        }

        // 4b. Upload ảnh mới
        if (newImages != null && !newImages.isEmpty()) {
            List<String> newImageUrls = newImages.stream()
                    .map(minioService::uploadFile)
                    .toList();
            finalImages.addAll(newImageUrls);
        }

        // Chỉ cập nhật nếu có sự thay đổi về ảnh (tránh xóa hết nếu client không gửi gì)
        // Logic ở đây: Nếu client gửi keepImages = rỗng VÀ không có ảnh mới -> Có thể hiểu là xóa hết ảnh?
        // Hoặc an toàn hơn: Nếu cả 2 đều null -> Giữ nguyên ảnh cũ.
        if (request.getKeepImages() != null || (newImages != null && !newImages.isEmpty())) {
            product.setImages(finalImages);
            // Update thumbnail bằng ảnh đầu tiên
            if (!finalImages.isEmpty()) {
                product.setThumbnail(finalImages.getFirst());
            }
        }

        product.setUpdatedAt(Instant.now());

        return productRepository.save(product);
    }

    public List<ProductResponse> getProductsByIds(List<String> ids) {

        List<Product> products = productRepository.findAllById(ids);

        List<InventoryStockResponse> stockList = new ArrayList<>();
        try {
            if (!products.isEmpty()) {
                stockList = inventoryClient.getBatchStock(ids);
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi Inventory Service", e);
        }

        Map<String, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(
                        InventoryStockResponse::getProductId,
                        InventoryStockResponse::getAvailableQuantity,
                        (existing, replacement) -> existing
                ));

        return products.stream()
                .map(product ->
                        ProductMapper.toResponse(
                                product,
                                stockMap.getOrDefault(product.getId(), 0)
                        )
                )
                .toList();
    }

}