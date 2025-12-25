package com.chien.agricultural.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component // Quan trọng: Đánh dấu là Bean để Inject vào SecurityConfig
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        // 1. Set Header & Status Code 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 2. Chuẩn bị nội dung JSON trả về
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("path", request.getRequestURI());

        // 3. Phân tích nguyên nhân lỗi để trả về thông báo tiếng Việt dễ hiểu
        String originalMessage = authException.getMessage();
        String friendlyMessage = "Truy cập bị từ chối. Vui lòng đăng nhập.";

        if (originalMessage != null) {
            if (originalMessage.contains("Jwt expired") || originalMessage.contains("expired")) {
                friendlyMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
            } else if (originalMessage.contains("Invalid") || originalMessage.contains("signature")) {
                friendlyMessage = "Token không hợp lệ hoặc đã bị thay đổi.";
            } else if (originalMessage.contains("No bearer token")) {
                friendlyMessage = "Không tìm thấy Token xác thực.";
            }
        }

        body.put("message", friendlyMessage);

        // (Tùy chọn) Gửi kèm lỗi gốc để debug nếu cần
        // body.put("debugMessage", originalMessage);

        // 4. Ghi JSON ra response stream bằng Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}