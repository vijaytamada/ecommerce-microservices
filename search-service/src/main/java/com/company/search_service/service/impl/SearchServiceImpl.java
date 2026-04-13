package com.company.search_service.service.impl;

import com.company.search_service.document.ProductDocument;
import com.company.search_service.repository.ProductSearchRepository;
import com.company.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductSearchRepository searchRepository;

    @Override
    public Page<ProductDocument> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return searchRepository.findAll(pageable);
        }
        return searchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);
    }

    @Override
    public Page<ProductDocument> searchByCategory(String categoryId, Pageable pageable) {
        return searchRepository.findByCategoryIdAndStatus(categoryId, "ACTIVE", pageable);
    }

    @Override
    public void indexProduct(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        String productId = (String) event.get("productId");

        if ("PRODUCT_DELETED".equals(type)) {
            removeProduct(productId);
            return;
        }

        ProductDocument doc = searchRepository.findById(productId).orElse(
                ProductDocument.builder().id(productId).build());

        if (event.get("productName") != null) doc.setName((String) event.get("productName"));
        if (event.get("categoryId") != null)  doc.setCategoryId((String) event.get("categoryId"));
        if (event.get("categoryName") != null) doc.setCategoryName((String) event.get("categoryName"));
        if (event.get("price") != null) doc.setPrice(new BigDecimal(event.get("price").toString()));
        if (event.get("status") != null) doc.setStatus((String) event.get("status"));
        doc.setIndexedAt(LocalDateTime.now());

        searchRepository.save(doc);
        log.info("Indexed product: id={}, event={}", productId, type);
    }

    @Override
    public void removeProduct(String productId) {
        searchRepository.deleteById(productId);
        log.info("Removed product from index: id={}", productId);
    }

    @Override
    public void updateRating(String productId, double newAvgRating, long totalReviews) {
        searchRepository.findById(productId).ifPresent(doc -> {
            doc.setAverageRating(newAvgRating);
            doc.setTotalReviews(totalReviews);
            searchRepository.save(doc);
            log.info("Updated rating for product={}: avg={}, total={}", productId, newAvgRating, totalReviews);
        });
    }
}
