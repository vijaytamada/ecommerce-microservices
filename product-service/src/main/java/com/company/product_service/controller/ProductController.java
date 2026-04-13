package com.company.product_service.controller;

import com.company.product_service.document.Product.ProductStatus;
import com.company.product_service.dto.request.ProductRequest;
import com.company.product_service.dto.response.ApiResponse;
import com.company.product_service.dto.response.ProductResponse;
import com.company.product_service.service.ProductService;
import com.company.product_service.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all active products (paginated)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Products fetched", productService.listProducts(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Product fetched", productService.getProduct(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        UUID sellerId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Product created", productService.createProduct(sellerId, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        UUID requesterId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Product updated",
                productService.updateProduct(id, requesterId, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change product status (ADMIN)")
    public ResponseEntity<ApiResponse<ProductResponse>> changeStatus(
            @PathVariable String id,
            @RequestParam ProductStatus status) {
        UUID requesterId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                productService.changeStatus(id, requesterId, status)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product (ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        UUID requesterId = HeaderUtils.getCurrentUserId();
        productService.deleteProduct(id, requesterId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsBySeller(
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.listProductsBySeller(sellerId, pageable)));
    }
}
