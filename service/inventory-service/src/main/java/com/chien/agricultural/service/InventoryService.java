package com.chien.agricultural.service;

import com.chien.agricultural.entity.Inventory;
import com.chien.agricultural.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // 1. Khởi tạo kho (Gọi khi Nông dân tạo sản phẩm bên Product Service)
    @Transactional
    public void initInventory(String productId, Integer quantity, Instant harvestDate) {
        if (inventoryRepository.existsByProductId(productId)) {
            return; // Đã có thì thôi
        }

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(0)
                .harvestDate(harvestDate)
                .createdAt(Instant.now())
                .build();

        inventoryRepository.save(inventory);
    }

    // 2. Check tồn kho (Xem có đủ hàng để bán không)
    public boolean isInStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));

        // Logic: Khả dụng = Tổng - Đã Giữ
        return (inventory.getQuantity() - inventory.getReservedQuantity()) >= quantity;
    }

    // 3. Trừ kho (Giữ hàng - Reserve) - Gọi khi khách bấm "Đặt hàng"
    @Transactional
    public void deductStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));

        int availableStock = inventory.getQuantity() - inventory.getReservedQuantity();

        if (availableStock < quantity) {
            throw new RuntimeException("Hết hàng! Chỉ còn " + availableStock + " sản phẩm.");
        }

        // Tăng lượng hàng đã giữ lên
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setUpdatedAt(Instant.now());
        inventoryRepository.save(inventory);
    }

    // 4. Hoàn kho (Restore) - Gọi khi hủy đơn
    @Transactional
    public void restoreStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        if (inventory.getReservedQuantity() < 0) inventory.setReservedQuantity(0);
        inventory.setUpdatedAt(Instant.now());

        inventoryRepository.save(inventory);
    }
}