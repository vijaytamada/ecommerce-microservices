package com.company.search_service.service;

import com.company.search_service.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SearchService {
    Page<ProductDocument> search(String query, Pageable pageable);
    Page<ProductDocument> searchByCategory(String categoryId, Pageable pageable);
    void indexProduct(Map<String, Object> productEvent);
    void removeProduct(String productId);
    void updateRating(String productId, double newAvgRating, long totalReviews);
}
