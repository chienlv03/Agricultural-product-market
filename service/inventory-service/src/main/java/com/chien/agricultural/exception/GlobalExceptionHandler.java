package com.chien.agricultural.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    // 1. Bắt lỗi Validate dữ liệu (@Valid, @NotNull...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", errors);
    }

    // 2. Bắt lỗi Custom App Exception
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex) {
        return buildResponse(ex.getStatus(), ex.getStatus().getReasonPhrase(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", "Đường dẫn API không tồn tại.");
    }

    // 6. Bắt lỗi RuntimeException chung chung
    // Lưu ý: Đặt cái này sau các lỗi Security để tránh việc nó nuốt mất lỗi Security
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    // 7. Bắt tất cả các lỗi còn lại (Lỗi hệ thống 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        // Nên dùng Logger thay vì System.out
        log.error("Lỗi hệ thống không mong muốn: ", ex);

        // Trả về thông báo chung chung để bảo mật
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Lỗi hệ thống không mong muốn: " + ex.getMessage());
    }

    // Hàm Utility để build response cho gọn code
    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, Object message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}