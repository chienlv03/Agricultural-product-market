package com.chien.agricultural.service;

import com.chien.agricultural.dto.response.UserResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.model.User;
import com.chien.agricultural.model.UserRole;
import com.chien.agricultural.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        // 1. Gọi Repo lấy tất cả user KHÔNG PHẢI là ADMIN
        Page<User> usersPage = userRepository.findAllByUserRoleNot(UserRole.ADMIN, pageable);

        // 2. Map từ Entity User sang DTO UserResponse
        return usersPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build());
    }

}