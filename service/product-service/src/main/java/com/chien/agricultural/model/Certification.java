package com.chien.agricultural.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Certification {
    private String type; // VietGAP
    private String code; // Mã số chứng nhận
    private String imageUrl; // Ảnh bằng chứng
}
