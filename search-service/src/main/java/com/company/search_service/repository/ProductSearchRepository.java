package com.company.search_service.repository;

import com.company.search_service.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    Page<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    Page<ProductDocument> findByCategoryIdAndStatus(String categoryId, String status, Pageable pageable);
}
