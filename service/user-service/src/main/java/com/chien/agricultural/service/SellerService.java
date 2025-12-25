package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.UpdateSellerProfileRequest;
import com.chien.agricultural.dto.response.SellerProfileResponse;
import com.chien.agricultural.dto.response.UserResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.mapper.SellerMapper;
import com.chien.agricultural.model.SellerProfile;
import com.chien.agricultural.model.User;
import com.chien.agricultural.model.UserRole;
import com.chien.agricultural.repository.SellerProfileRepository;
import com.chien.agricultural.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final MinioService minioService;
    private final UserService userService;

    @Transactional
    public UserResponse updateSellerProfile(
            UpdateSellerProfileRequest request,
            MultipartFile avatar,
            MultipartFile idCardFrontFile, // Thêm ảnh mặt trước
            MultipartFile idCardBackFile,  // Thêm ảnh mặt sau
            List<MultipartFile> farmImages // Ảnh vườn
    ) {
        // 1. Lấy User hiện tại
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // ---------------------------------------------------------
        // BƯỚC 1: CẬP NHẬT BẢNG USER (Thông tin chung)
        // ---------------------------------------------------------
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        // Xử lý Avatar (Upload MinIO)
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = minioService.uploadFile(avatar);
            user.setAvatarUrl(avatarUrl);
        }

        // Lưu User
        userRepository.save(user);

        // ---------------------------------------------------------
        // BƯỚC 2: CẬP NHẬT BẢNG SELLER_PROFILE (Thông tin bán hàng)
        // ---------------------------------------------------------
        // Chỉ cập nhật nếu user có role SELLER (hoặc bạn có thể cho phép tự nâng cấp role ở đây tùy logic)
        if (user.getUserRole() == UserRole.SELLER) {
            SellerProfile profile = sellerProfileRepository.findById(userId).orElse(
                    SellerProfile.builder().user(user).build()
            );

            // -- A. Thông tin Text cơ bản --
            if (request.getTaxCode() != null) profile.setTaxCode(request.getTaxCode());
            if (request.getFarmName() != null) profile.setFarmName(request.getFarmName());
            if (request.getFarmDescription() != null) profile.setFarmDescription(request.getFarmDescription());
            if (request.getFarmAddress() != null) profile.setFarmAddress(request.getFarmAddress());

            // -- B. Xử lý Ảnh CCCD/CMND (Upload MinIO) --
            if (idCardFrontFile != null && !idCardFrontFile.isEmpty()) {
                String frontUrl = minioService.uploadFile(idCardFrontFile);
                profile.setIdCardFront(frontUrl);
            }
            if (idCardBackFile != null && !idCardBackFile.isEmpty()) {
                String backUrl = minioService.uploadFile(idCardBackFile);
                profile.setIdCardBack(backUrl);
            }

            // -- C. Xử lý Ảnh Vườn (Farm Photos) --
            // Logic: Nếu gửi ảnh mới lên thì upload và lưu đè (hoặc add thêm tùy nghiệp vụ)
            if (farmImages != null && !farmImages.isEmpty()) {
                List<String> imageUrls = farmImages.stream()
                        .map(minioService::uploadFile)
                        .toList();
                profile.setFarmPhotos(imageUrls);
            }

            // -- D. Xử lý Tọa độ (PostGIS Point) --
            // Cần cả Lat và Lon mới tạo được Point
            if (request.getLatitude() != null && request.getLongitude() != null) {
                GeometryFactory geometryFactory = new GeometryFactory();
                // Lưu ý: JTS nhận (Longitude, Latitude) - Kinh độ trước, Vĩ độ sau
                Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                point.setSRID(4326); // Set hệ tọa độ WGS84 (GPS chuẩn)
                profile.setLocation(point);
            }

            // -- E. Xử lý Chứng chỉ (Certifications - JSONB) --
            if (request.getCertifications() != null) {
                // Lưu ý: Đảm bảo Entity SellerProfile đã cấu hình mapping JSONB cho field này
                profile.setCertifications(request.getCertifications());
            }

            // Lưu Profile
            sellerProfileRepository.save(profile);
        }

        return userService.getMyProfile();
    }

    public List<SellerProfileResponse> getAllSellers() {
        return userRepository.findAllByUserRole(UserRole.SELLER)
                .stream()
                .map(user -> {
                    SellerProfile profile =
                            sellerProfileRepository.findById(user.getId()).orElse(null);
                    return SellerMapper.toResponse(user, profile);
                })
                .toList();
    }


    public SellerProfileResponse getSellerById() {
        String sellerId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException("Seller not found", HttpStatus.NOT_FOUND));

        SellerProfile profile = sellerProfileRepository.findById(sellerId)
                .orElse(null);

        return SellerMapper.toResponse(user, profile);
    }

}
