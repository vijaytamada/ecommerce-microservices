package com.company.search_service.controller;

import com.company.search_service.document.ProductDocument;
import com.company.search_service.dto.response.ApiResponse;
import com.company.search_service.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text product search APIs")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/products")
    @Operation(summary = "Full-text search for products")
    public ResponseEntity<ApiResponse<Page<ProductDocument>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "averageRating") String sortBy) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return ResponseEntity.ok(ApiResponse.success("Search results", searchService.search(q, pageable)));
    }

    @GetMapping("/products/category/{categoryId}")
    @Operation(summary = "Browse products by category")
    public ResponseEntity<ApiResponse<Page<ProductDocument>>> searchByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("averageRating").descending());
        return ResponseEntity.ok(ApiResponse.success("Category results", searchService.searchByCategory(categoryId, pageable)));
    }
}
