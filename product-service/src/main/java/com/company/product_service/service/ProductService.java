package com.company.product_service.service;

import com.company.product_service.document.Product.ProductStatus;
import com.company.product_service.dto.request.CategoryRequest;
import com.company.product_service.dto.request.ProductRequest;
import com.company.product_service.dto.response.CategoryResponse;
import com.company.product_service.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(UUID sellerId, ProductRequest request);

    ProductResponse getProduct(String productId);

    Page<ProductResponse> listProducts(Pageable pageable);

    Page<ProductResponse> listProductsByCategory(String categoryId, Pageable pageable);

    Page<ProductResponse> listProductsBySeller(UUID sellerId, Pageable pageable);

    ProductResponse updateProduct(String productId, UUID requesterId, ProductRequest request);

    ProductResponse changeStatus(String productId, UUID requesterId, ProductStatus status);

    void deleteProduct(String productId, UUID requesterId);

    /* Categories */
    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> listCategories();

    CategoryResponse getCategory(String categoryId);
}
