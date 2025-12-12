package com.chien.agricultural.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {
    private String productId;
    private Integer quantity;
}
