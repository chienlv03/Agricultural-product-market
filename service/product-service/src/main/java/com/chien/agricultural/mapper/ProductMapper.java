package com.chien.agricultural.mapper;

import com.chien.agricultural.dto.response.ProductResponse;
import com.chien.agricultural.dto.response.ProductVariantResponse;
import com.chien.agricultural.dto.response.SellerProfileResponse;
import com.chien.agricultural.model.Product;
import com.chien.agricultural.model.ProductVariant;
import com.chien.agricultural.model.SellerInfo;

import java.math.BigDecimal;
import java.util.List;

public class ProductMapper {
    public static ProductResponse toResponse(Product product, Integer availableQuantity) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice() != null
                        ? BigDecimal.valueOf(product.getPrice())
                        : null)
                .unit(product.getUnit())
                .thumbnail(product.getThumbnail())
                .availableQuantity(availableQuantity)
                .sellerProfileResponse(mapSeller(product.getSeller()))
                .variants(mapVariants(product.getVariants()))
                .build();
    }

    private static SellerProfileResponse mapSeller(SellerInfo seller) {
        if (seller == null) return null;

        return SellerProfileResponse.builder()
                .sellerId(seller.getSellerId())
                .phone(seller.getPhone())
                .fullName(seller.getFullName())
                .email(seller.getEmail())
                .avatarUrl(seller.getAvatarUrl())
                .idCardFront(seller.getIdCardFront())
                .idCardBack(seller.getIdCardBack())
                .taxCode(seller.getTaxCode())
                .farmName(seller.getFarmName())
                .farmDescription(seller.getFarmDescription())
                .farmAddress(seller.getFarmAddress())
                .farmPhotos(seller.getFarmPhotos())
                .latitude(seller.getLatitude())
                .longitude(seller.getLongitude())
                .build();
    }

    private static List<ProductVariantResponse> mapVariants(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return List.of();

        return variants.stream()
                .map(v -> ProductVariantResponse.builder()
                        .id(v.getVariantId())
                        .sku(v.getSku())
                        .name(v.getName())
                        .price(v.getPrice() != null
                                ? BigDecimal.valueOf(v.getPrice())
                                : null)
                        .build()
                )
                .toList();
    }
}
