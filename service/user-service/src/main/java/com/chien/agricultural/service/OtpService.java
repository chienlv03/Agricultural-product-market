package com.chien.agricultural.service;

import com.chien.agricultural.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
//    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String OTP_PREFIX = "OTP:";

    public String generateAndSendOtp(String phoneNumber) {
        // 1. Sinh OTP 6 số
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // 2. Lưu Redis (hết hạn sau 5 phút)
        redisTemplate.opsForValue().set(OTP_PREFIX + phoneNumber, otp, Duration.ofMinutes(5));

        // Log tạm để test trên Console nếu chưa chạy Notification Service
        System.out.println("DEBUG OTP for " + phoneNumber + ": " + otp);

        return otp;
    }

    public void validateOtp(String phoneNumber, String inputOtp) {
        String cachedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + phoneNumber);

        if (cachedOtp == null) {
            throw new AppException("Mã OTP đã hết hạn hoặc bạn chưa yêu cầu gửi mã. Vui lòng thử lại!", HttpStatus.BAD_REQUEST);
        }

        if (!cachedOtp.equals(inputOtp)) {
            throw new AppException("Mã OTP không chính xác", HttpStatus.BAD_REQUEST);
        }

        // Xóa OTP sau khi dùng xong để tránh dùng lại
        redisTemplate.delete(OTP_PREFIX + phoneNumber);
    }
}