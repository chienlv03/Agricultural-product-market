package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.LoginRequest;
import com.chien.agricultural.dto.request.RegisterRequest;
import com.chien.agricultural.dto.response.AuthResponse;
import com.chien.agricultural.dto.response.OtpResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.model.User;
import com.chien.agricultural.model.UserStatus;
import com.chien.agricultural.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        otpService.validateOtp(request.getPhoneNumber(), request.getOtpCode());

        if (userRepository.existsByPhone(request.getPhoneNumber())) {
            throw new AppException("Số điện thoại này đã được đăng ký!", HttpStatus.BAD_REQUEST);
        }

        String keycloakId = null;

        try {
            // 1. Create user on Keycloak
            keycloakId = identityService.createKeycloakUser(
                    request.getPhoneNumber(),
                    request.getUserRole().name()
            );

            // 2. Save DB
            User user = User.builder()
                    .id(keycloakId)
                    .phone(request.getPhoneNumber())
                    .userRole(request.getUserRole())
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);

            // 3. Login
            return identityService.exchangeToken(request.getPhoneNumber());

        } catch (Exception e) {

            // ROLLBACK KEYCLOAK
            if (keycloakId != null) {
                identityService.deleteKeycloakUser(keycloakId);
            }

            throw e;
        }
    }


    public AuthResponse login(LoginRequest request) {
        // 1. Validate OTP
        otpService.validateOtp(request.getPhoneNumber(), request.getOtpCode());

        // 2. Check DB xem có user chưa
        userRepository.findByPhone(request.getPhoneNumber())
                .orElseThrow(() -> new AppException("Số điện thoại chưa đăng ký! Vui lòng chọn Đăng ký.", HttpStatus.BAD_REQUEST));

        // 3. Lấy Token từ Keycloak
        return identityService.exchangeToken(request.getPhoneNumber());
    }

    // ...
    public AuthResponse refreshToken(String refreshToken) {
        return identityService.refreshToken(refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Gọi Keycloak để hủy Session (Quan trọng)
        if (refreshToken != null) {
            identityService.logout(refreshToken);
        }

        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(true) // Quan trọng nếu chạy HTTPS (Production)
                .sameSite("None") // Quan trọng nếu Frontend và Backend khác domain/port
                .maxAge(0) // Set thời gian sống = 0 để xóa ngay lập tức
                .build();

        // 2. Xóa Refresh Token (nếu có)
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .build();

        // 3. Gắn vào Header
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());
    }
}