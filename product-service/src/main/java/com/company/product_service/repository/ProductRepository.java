package com.company.product_service.repository;

import com.company.product_service.document.Product;
import com.company.product_service.document.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(String categoryId, ProductStatus status, Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);
}
