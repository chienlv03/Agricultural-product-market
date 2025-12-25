package com.chien.agricultural.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private static final double EARTH_RADIUS = 6371; // Bán kính trái đất (km)

    /**
     * Tính phí ship dựa trên Khoảng cách và Cân nặng
     */
    @Override
    public BigDecimal calculateFee(Double lat1, Double lon1, Double lat2, Double lon2, Double totalWeightInKg) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return BigDecimal.valueOf(30000); // Mặc định nếu lỗi tọa độ
        }

        // 1. Tính khoảng cách (KM)
        double distance = calculateDistance(lat1, lon1, lat2, lon2);

        // 2. Cước cơ bản theo khoảng cách
        double baseFee = 15000;
        if (distance > 10) baseFee += (distance - 10) * 1000; // Xa trên 10km, mỗi km thêm 1k
        if (distance > 50) baseFee += (distance - 50) * 2000; // Xa trên 50km, mỗi km thêm 2k

        // 3. Phụ phí cân nặng (Nếu trên 5kg, mỗi kg thêm 5k)
        double weightFee = 0;
        if (totalWeightInKg > 5.0) {
            weightFee = (totalWeightInKg - 5.0) * 5000;
        }

        double total = baseFee + weightFee;

        // Làm tròn lên hàng nghìn (VD: 15400 -> 16000)
        return BigDecimal.valueOf(Math.ceil(total / 1000) * 1000);
    }

    // Công thức Haversine
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
