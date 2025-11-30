package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.LoginRequest;
import com.chien.agricultural.dto.request.LogoutRequest;
import com.chien.agricultural.dto.request.RegisterRequest;
import com.chien.agricultural.dto.response.AuthResponse;
import com.chien.agricultural.dto.response.OtpResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.model.User;
import com.chien.agricultural.model.UserStatus;
import com.chien.agricultural.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final IdentityService identityService;
    private final UserRepository userRepository;

    public OtpResponse sendOtp(String phoneNumber) {
        return OtpResponse.builder()
                .otp(otpService.generateAndSendOtp(phoneNumber))
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Validate OTP
        otpService.validateOtp(request.getPhoneNumber(), request.getOtpCode());

        // 2. Check DB
        if (userRepository.existsByPhone(request.getPhoneNumber())) {
            throw new AppException("Số điện thoại này đã được đăng ký!", HttpStatus.BAD_REQUEST);
        }

        // 3. Tạo User trên Keycloak
        String keycloakId = identityService.createKeycloakUser(request.getPhoneNumber(), request.getUserRole().name());

        // 4. Lưu vào DB PostgreSQL
        User user = new User();
        user.setId(keycloakId); // Đồng bộ ID
        user.setPhone(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        user.setUserRole(request.getUserRole());
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        // 5. Tự động Login luôn
        return identityService.exchangeToken(request.getPhoneNumber());
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Validate OTP
        otpService.validateOtp(request.getPhoneNumber(), request.getOtpCode());

        // 2. Check DB xem có user chưa
        User user = userRepository.findByPhone(request.getPhoneNumber())
                .orElseThrow(() -> new AppException("Số điện thoại chưa đăng ký! Vui lòng chọn Đăng ký.", HttpStatus.BAD_REQUEST));

        // 3. Lấy Token từ Keycloak
        return identityService.exchangeToken(request.getPhoneNumber());
    }

    public void logout(LogoutRequest request) {
        identityService.logout(request.getRefreshToken());
    }
}