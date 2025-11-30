package com.chien.agricultural.client;

import com.chien.agricultural.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/users/me")
    UserResponse getMyProfile();
}
