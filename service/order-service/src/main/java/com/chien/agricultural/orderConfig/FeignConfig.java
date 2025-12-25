package com.chien.agricultural.orderConfig;

import feign.RequestInterceptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 1. Ưu tiên lấy từ Header (Cách cũ)
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);
                return;
            }

            // 2. Nếu Header không có, tìm trong Cookie (Cách mới)
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        // Chuyển đổi Cookie thành Header "Authorization: Bearer ..."
                        // để gửi sang User Service (vì User Service ưu tiên đọc Header)
                        requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + cookie.getValue());
                        return;
                    }
                }
            }
            log.error("Không tìm thấy thông tin xác thực trong Header hoặc Cookie");
        };
    }
}