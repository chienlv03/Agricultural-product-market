package com.chien.agricultural.service;

import java.math.BigDecimal;

public interface ShippingService {

    BigDecimal calculateFee(Double lat1, Double lon1, Double lat2, Double lon2, Double totalWeightInKg);
}
