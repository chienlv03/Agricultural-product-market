package com.chien.agricultural.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Import các class Security exception
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    // 1. Bắt lỗi App Exception (Giữ nguyên)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex) {
        log.error("Lỗi ứng dụng: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, ex.getStatus());
    }

    // 2. BẮT BUỘC THÊM: Bắt lỗi 403 Forbidden (Không có quyền truy cập)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Lỗi truy cập bị từ chối: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value()); // 403
        body.put("error", "Forbidden");
        body.put("message", "Bạn không có quyền truy cập tài nguyên này.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // 3. BẮT BUỘC THÊM: Bắt lỗi 401 Unauthorized (Token lỗi/hết hạn)
    // Lưu ý: Thường lỗi này bị Filter chặn trước khi đến đây, nhưng nếu lọt vào Controller thì phải bắt.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        log.error("Lỗi xác thực: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value()); // 401
        body.put("error", "Unauthorized");
        body.put("message", "Xác thực thất bại (Vui lòng đăng nhập lại).");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // 4. Bắt các lỗi còn lại (Giữ nguyên nhưng sửa message để debug dễ hơn)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnwantedException(Exception ex) {
        log.error("Lỗi hệ thống không mong muốn: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        // Không nên trả về ex.getMessage() cho client ở môi trường Product vì lý do bảo mật
        // Nhưng môi trường Dev thì ok.
        body.put("message", "Lỗi hệ thống: " + ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}