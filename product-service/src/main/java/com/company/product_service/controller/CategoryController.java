package com.company.product_service.controller;

import com.company.product_service.dto.request.CategoryRequest;
import com.company.product_service.dto.response.ApiResponse;
import com.company.product_service.dto.response.CategoryResponse;
import com.company.product_service.dto.response.ProductResponse;
import com.company.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management APIs")
public class CategoryController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched", productService.listCategories()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Category fetched", productService.getCategory(id)));
    }

    @PostMapping
    @Operation(summary = "Create a category (ADMIN)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category created", productService.createCategory(request)));
    }

    @GetMapping("/{id}/products")
    @Operation(summary = "List products in a category")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.listProductsByCategory(id, pageable)));
    }
}
