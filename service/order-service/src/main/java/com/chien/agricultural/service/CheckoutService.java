package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.CheckoutPreviewRequest;
import com.chien.agricultural.dto.response.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse previewOrder(CheckoutPreviewRequest request);
}
