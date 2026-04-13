package com.company.review_service.repository;

import com.company.review_service.document.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends MongoRepository<Review, String> {
    Page<Review> findByProductIdAndStatus(String productId, String status, Pageable pageable);
    Page<Review> findByUserId(UUID userId, Pageable pageable);
    Optional<Review> findByProductIdAndUserId(String productId, UUID userId);
    long countByProductId(String productId);
}
