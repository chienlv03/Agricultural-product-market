package com.chien.agricultural.service;

import com.chien.agricultural.dto.DeductStockRequest;
import com.chien.agricultural.dto.InventoryStockResponse;
import com.chien.agricultural.entity.Inventory;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
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
    @Transactional(rollbackFor = Exception.class)
    public void deductStockBatch(List<DeductStockRequest> requests) {
        log.info("Bắt đầu trừ kho cho {} sản phẩm", requests.size());

        for (DeductStockRequest req : requests) {
            // Tìm kho theo ProductID (Khóa dòng để tránh tranh chấp)
            Inventory inventory = inventoryRepository.findByProductIdLocked(req.getProductId())
                    .orElseThrow(() -> new AppException("Không tìm thấy thông tin kho cho sản phẩm: " + req.getProductId(), HttpStatus.NOT_FOUND));

            // Kiểm tra số lượng
            if (inventory.getQuantity() < req.getQuantity()) {
                throw new AppException("Sản phẩm (ID: " + req.getProductId() + ") không đủ số lượng tồn kho.", HttpStatus.BAD_REQUEST);
            }

            // Trừ kho
            inventory.setQuantity(inventory.getQuantity() - req.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    // 4. Hoàn kho (Restore) - Gọi khi hủy đơn
    @Transactional
    public void restoreStock(List<DeductStockRequest> items) {
        for (DeductStockRequest item : items) {
            inventoryRepository.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryStockResponse> findByProductIdIn(List<String> productIds) {

        List<Inventory> inventories =
                inventoryRepository.findByProductIdIn(productIds);

        return inventories.stream()
                .map(inv -> InventoryStockResponse.builder()
                        .productId(inv.getProductId())
                        .availableQuantity(
                                inv.getQuantity() - inv.getReservedQuantity()
                        )
                        .build())
                .toList();
    }



}