package com.chien.agricultural.config;

import com.chien.agricultural.exception.JwtAuthenticationEntryPoint;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Sử dụng cấu hình CORS mặc định
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF vì dùng API Stateless
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai các API Auth
                        .requestMatchers("/api/v1/**").permitAll()
                        // Các API còn lại bắt buộc phải có Token
                        .anyRequest().authenticated()
                )
                // Cấu hình Resource Server để đọc JWT từ Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)

                        // --- THÊM DÒNG QUAN TRỌNG NÀY ---
                        // Bảo Spring: "Đừng tìm header nữa, dùng cái resolver của tôi để tìm trong cookie"
                        .bearerTokenResolver(cookieBearerTokenResolver())
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public BearerTokenResolver cookieBearerTokenResolver() {
        return (request) -> {
            // 1. Ưu tiên tìm trong Header trước (để Postman vẫn test được dễ dàng)
            String header = request.getHeader("Authorization");
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                return header.substring(7);
            }

            // 2. Nếu Header không có, tìm trong Cookie "accessToken"
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null; // Không tìm thấy token
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. Tạo Decoder từ JWK Set URI (để verify chữ ký)
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // 2. Định nghĩa Validator: Chỉ kiểm tra Timestamp (hết hạn chưa)
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

        // (Nếu muốn kiểm tra thêm gì đó thì add vào đây, nhưng KHÔNG add IssuerValidator)

        // 3. Gán Validator mới vào Decoder (Ghi đè Validator mặc định khắt khe của Spring)
        jwtDecoder.setJwtValidator(withTimestamp);

        return jwtDecoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}