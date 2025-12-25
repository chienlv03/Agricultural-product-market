package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.UpdateSellerProfileRequest;
import com.chien.agricultural.dto.response.SellerProfileResponse;
import com.chien.agricultural.dto.response.UserResponse;
import com.chien.agricultural.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final SellerService sellerService;

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateProfile(
            @RequestPart("data") UpdateSellerProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "idCardFront", required = false) MultipartFile idCardFront,
            @RequestPart(value = "idCardBack", required = false) MultipartFile idCardBack,
            @RequestPart(value = "farmImages", required = false) List<MultipartFile> farmImages
    ) {
        UserResponse userResponse = sellerService.updateSellerProfile(request, avatar, idCardFront, idCardBack, farmImages);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<SellerProfileResponse> getSellerById() {

        return ResponseEntity.ok(sellerService.getSellerById());
    }
}
