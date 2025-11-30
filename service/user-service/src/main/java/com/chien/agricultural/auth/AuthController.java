package com.chien.agricultural.auth;

import com.chien.agricultural.dto.request.LoginRequest;
import com.chien.agricultural.dto.request.OtpRequest;
import com.chien.agricultural.dto.request.RegisterRequest;
import com.chien.agricultural.dto.response.AuthResponse;
import com.chien.agricultural.dto.response.OtpResponse;
import com.chien.agricultural.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Bước 1: Client gọi API này để lấy mã OTP
    @PostMapping("/otp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody OtpRequest request) {
        OtpResponse otp = authService.sendOtp(request.getPhoneNumber());
        return ResponseEntity.ok(otp);
    }

    // Bước 2a: Đăng ký (Có chọn Role)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        // 1. Gọi service lấy token (trả về Map hoặc Object chứa access_token, refresh_token)
        // Lưu ý: Bạn cần ép kiểu về Map hoặc DTO tương ứng từ kết quả của authService.register
        AuthResponse tokenData = authService.register(request);

        // 2. Set Cookies
        setTokenCookies(response, tokenData);

        return ResponseEntity.ok(tokenData); // Vẫn trả body để frontend biết thông tin user nếu cần
    }

    // Bước 2b: Đăng nhập (Không cần chọn Role)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse tokenData = authService.login(request);

        setTokenCookies(response, tokenData);

        return ResponseEntity.ok(tokenData);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Xóa Cookie bằng cách set maxAge = 0
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "").path("/").maxAge(0).build();
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "").path("/").maxAge(0).build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        // (Tùy chọn) Gọi thêm logic logout Keycloak nếu cần
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    private void setTokenCookies(HttpServletResponse response, AuthResponse tokenData) {
        // Lấy dữ liệu bằng getter chuẩn Java (không cần get("key") nữa)
        String accessToken = tokenData.getAccessToken();
        String refreshToken = tokenData.getRefreshToken();
        // Keycloak trả về giây, cookie maxAge cũng tính bằng giây -> OK
        int expiresIn = tokenData.getExpiresIn().intValue();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false) // localhost
                .path("/")
                .maxAge(expiresIn)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}