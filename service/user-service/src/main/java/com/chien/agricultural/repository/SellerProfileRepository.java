package com.chien.agricultural.repository;

import com.chien.agricultural.model.SellerProfile;
import com.chien.agricultural.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, String> {
    @Query(value = """
        SELECT s.* FROM user_service.seller_profiles s
        WHERE ST_DWithin(
            s.location, 
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), 
            :radiusInMeters
        )
        ORDER BY ST_Distance(
            s.location, 
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)
        ) ASC
        """, nativeQuery = true)
    List<SellerProfile> findSellersNearBy(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusInMeters") double radiusInMeters
    );
}
