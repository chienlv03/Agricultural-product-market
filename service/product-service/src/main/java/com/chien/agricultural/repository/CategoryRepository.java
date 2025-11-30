package com.chien.agricultural.repository;

import com.chien.agricultural.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, Integer> {
    List<Category> findByIsActiveTrue();
}
