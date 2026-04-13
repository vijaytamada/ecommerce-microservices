package com.company.product_service.repository;

import com.company.product_service.document.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    boolean existsByName(String name);
}
