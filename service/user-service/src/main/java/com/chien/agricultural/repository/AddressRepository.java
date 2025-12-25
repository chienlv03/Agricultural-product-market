package com.chien.agricultural.repository;

import com.chien.agricultural.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, String> {

    // Lấy danh sách địa chỉ của user
    List<Address> findByUserId(String userId);

    // Reset tất cả địa chỉ của user về không mặc định
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultAddress(@Param("userId") String userId);
}
