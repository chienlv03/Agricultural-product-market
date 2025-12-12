package com.chien.agricultural.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "categories")
public class Category {
    @Id
    @Generated()
    private String id;
    private String name;
    private String slug;
    private String image;
    private String description;
    private Integer parentId;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
