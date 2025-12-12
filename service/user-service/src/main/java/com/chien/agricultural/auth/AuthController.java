package com.chien.agricultural.auth;

import com.chien.agricultural.dto.request.LoginRequest;
import com.chien.agricultural.dto.request.OtpRequest;
import com.chien.agricultural.dto.request.RegisterRequest;
import com.chien.agricultural.dto.response.AuthResponse;
import com.chien.agricultural.dto.response.OtpResponse;
import com.chien.agricultural.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        AuthResponse tokenData = authService.register(request);

        // 2. Set Cookies
        setTokenCookies(response, tokenData);

        return ResponseEntity.ok(tokenData);
    }

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

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. Tìm Refresh Token trong Cookie
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Nếu không có refresh token -> Báo lỗi 401 để Frontend logout luôn
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 2. Gọi Keycloak lấy token mới
            AuthResponse newTokenData = authService.refreshToken(refreshToken);

            // 3. QUAN TRỌNG: Set lại Cookie mới vào trình duyệt
            setTokenCookies(response, newTokenData);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Nếu refresh token cũng hết hạn -> Xóa cookie -> Bắt đăng nhập lại
            ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "").path("/").maxAge(0).build();
            ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "").path("/").maxAge(0).build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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