package com.chien.agricultural.repository;


import com.chien.agricultural.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);
    boolean existsByProductId(String productId);

    List<Inventory> findByProductIdIn(List<String> productIds);

    @Modifying // Bắt buộc có annotation này với các lệnh UPDATE/DELETE
    @Query(value = "UPDATE inventory " +
            "SET reserved_quantity = reserved_quantity - :quantity " +
            "AND updated_at = CURRENT_TIMESTAMP " +
            "WHERE product_id = :productId",
            nativeQuery = true)
    void restoreStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdAndVariantIdLocked(@Param("productId") String productId, @Param("variantId") String variantId);

    // Tìm kho của sản phẩm thường (variantId is null)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdLocked(@Param("productId") String productId);
}