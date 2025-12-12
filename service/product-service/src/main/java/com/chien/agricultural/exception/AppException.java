package com.chien.agricultural.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    // Constructor nhận vào Message và HTTP Status
    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // (Tùy chọn) Constructor nhận vào ErrorCode nếu bạn dùng Enum
    // public AppException(ErrorCode errorCode) {
    //     super(errorCode.getMessage());
    //     this.status = errorCode.getStatus();
    // }
}