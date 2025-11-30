package com.chien.agricultural.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certification implements Serializable {
    private String type;       // VietGAP, GlobalGAP, OCOP...
    private String url;        // Link ảnh/PDF chứng nhận
    private LocalDate issuedAt; // Ngày cấp
    private LocalDate expiredAt;// Ngày hết hạn
}