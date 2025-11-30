package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.UpdateSellerProfileRequest;
import com.chien.agricultural.dto.response.UserResponse;
import com.chien.agricultural.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping(value = "/seller/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateSellerProfile(
            @RequestPart("data") UpdateSellerProfileRequest request, // JSON data
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "images", required = false) List<MultipartFile> images // File ảnh
    ) {
        return ResponseEntity.ok(userService.updateSellerProfile(request, avatar, images));
    }
}