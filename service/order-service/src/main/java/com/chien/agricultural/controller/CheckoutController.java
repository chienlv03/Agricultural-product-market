package com.chien.agricultural.controller;

import com.chien.agricultural.dto.request.CheckoutPreviewRequest;
import com.chien.agricultural.dto.response.CheckoutResponse;
import com.chien.agricultural.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preview")
    public CheckoutResponse previewOrder(@RequestBody CheckoutPreviewRequest request) {
        return checkoutService.previewOrder(request);
    }
}
