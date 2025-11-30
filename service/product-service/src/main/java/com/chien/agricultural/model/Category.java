package com.chien.agricultural.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    private Integer id;
    private String name;
    private String slug;
    private String image;
    private String description;
    private Integer parentId;
    private Boolean isActive;
}
