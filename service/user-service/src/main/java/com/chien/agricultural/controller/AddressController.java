package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.AddressRequest;
import com.chien.agricultural.dto.response.AddressResponse;
import com.chien.agricultural.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // 1. Lấy danh sách
    @GetMapping("/me")
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    // 2. Thêm mới
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@RequestBody @Valid AddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(request));
    }

    // 3. Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable String id,
            @RequestBody @Valid AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    // 4. Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Lấy chi tiết địa chỉ
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable String id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }
}