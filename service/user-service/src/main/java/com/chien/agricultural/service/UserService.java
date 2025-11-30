package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.UpdateSellerProfileRequest;
import com.chien.agricultural.dto.response.UserResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.model.SellerProfile;
import com.chien.agricultural.model.User;
import com.chien.agricultural.model.UserRole;
import com.chien.agricultural.repository.SellerProfileRepository;
import com.chien.agricultural.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final MinioService minioService;

    public UserResponse getMyProfile() {
        // 1. Lấy User ID từ Security Context (Do JWT Filter đã giải mã token và đặt vào đây)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm user trong DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND));

        // 3. Map Entity sang DTO trả về
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .build();
    }

    // Sửa lại chữ ký hàm: thêm MultipartFile avatar
    @Transactional
    public UserResponse updateSellerProfile(UpdateSellerProfileRequest request,
                                            MultipartFile avatar,
                                            List<MultipartFile> farmImages) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // 1. Cập nhật thông tin cơ bản
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        // --- 2. XỬ LÝ AVATAR (MỚI THÊM) ---
        if (avatar != null && !avatar.isEmpty()) {
            // Upload lên MinIO
            String avatarUrl = minioService.uploadFile(avatar);
            // Lưu URL vào bảng User
            user.setAvatarUrl(avatarUrl);
        }

        // Lưu bảng User trước
        userRepository.save(user);

        // 3. Xử lý thông tin Nông dân (SellerProfile)
        if (user.getUserRole() == UserRole.SELLER) {
            SellerProfile profile = sellerProfileRepository.findById(userId).orElse(
                    SellerProfile.builder().user(user).verified(false).build()
            );

            if (request.getFarmName() != null) profile.setFarmName(request.getFarmName());
            if (request.getFarmDescription() != null) profile.setFarmDescription(request.getFarmDescription());
            if (request.getFarmAddress() != null) profile.setFarmAddress(request.getFarmAddress());

            // Xử lý ảnh vườn (nếu có gửi lên thì upload và lưu)
            if (farmImages != null && !farmImages.isEmpty()) {
                List<String> imageUrls = farmImages.stream()
                        .map(minioService::uploadFile)
                        .toList();
                profile.setFarmPhotos(imageUrls);
            }
            sellerProfileRepository.save(profile);
        }

        return getMyProfile();
    }
}