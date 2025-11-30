package com.chien.agricultural.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. Cho phép Credentials (Cookie, Auth Header)
        config.setAllowCredentials(true);

        // 2. Cho phép Origin cụ thể (Frontend của bạn)
        // Lưu ý: Khi setAllowCredentials(true) thì KHÔNG ĐƯỢC dùng "*"
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 3. Cho phép tất cả các Header (Authorization, Content-Type...)
        config.setAllowedHeaders(List.of("*"));

        // 4. Cho phép các Method (GET, POST, PUT, DELETE...)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Áp dụng cho tất cả các đường dẫn API
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}