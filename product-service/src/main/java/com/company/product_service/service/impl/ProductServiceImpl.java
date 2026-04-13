package com.company.product_service.service.impl;

import com.company.product_service.document.Category;
import com.company.product_service.document.Product;
import com.company.product_service.document.Product.ProductStatus;
import com.company.product_service.dto.request.CategoryRequest;
import com.company.product_service.dto.request.ProductRequest;
import com.company.product_service.dto.response.CategoryResponse;
import com.company.product_service.dto.response.ProductResponse;
import com.company.product_service.exception.ResourceNotFoundException;
import com.company.product_service.messaging.publisher.ProductEventPublisher;
import com.company.product_service.repository.CategoryRepository;
import com.company.product_service.repository.ProductRepository;
import com.company.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductEventPublisher eventPublisher;

    @Override
    public ProductResponse createProduct(UUID sellerId, ProductRequest request) {
        Category category = findCategoryOrThrow(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .sellerId(sellerId)
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .images(request.getImages())
                .attributes(request.getAttributes())
                .status(ProductStatus.ACTIVE)
                .build();

        product = productRepository.save(product);
        eventPublisher.publishProductCreated(product);
        log.info("Product created: id={}", product.getId());
        return toResponse(product);
    }

    @Override
    public ProductResponse getProduct(String productId) {
        return toResponse(findProductOrThrow(productId));
    }

    @Override
    public Page<ProductResponse> listProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable).map(this::toResponse);
    }

    @Override
    public Page<ProductResponse> listProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ProductResponse> listProductsBySeller(UUID sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable).map(this::toResponse);
    }

    @Override
    public ProductResponse updateProduct(String productId, UUID requesterId, ProductRequest request) {
        Product product = findProductOrThrow(productId);

        if (request.getName()        != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice()       != null) product.setPrice(request.getPrice());
        if (request.getImages()      != null) product.setImages(request.getImages());
        if (request.getAttributes()  != null) product.setAttributes(request.getAttributes());

        if (request.getCategoryId() != null) {
            Category category = findCategoryOrThrow(request.getCategoryId());
            product.setCategoryId(category.getId());
            product.setCategoryName(category.getName());
        }

        product = productRepository.save(product);
        eventPublisher.publishProductUpdated(product);
        return toResponse(product);
    }

    @Override
    public ProductResponse changeStatus(String productId, UUID requesterId, ProductStatus status) {
        Product product = findProductOrThrow(productId);
        product.setStatus(status);
        product = productRepository.save(product);
        eventPublisher.publishStatusChanged(product);
        return toResponse(product);
    }

    @Override
    public void deleteProduct(String productId, UUID requesterId) {
        Product product = findProductOrThrow(productId);
        productRepository.delete(product);
        eventPublisher.publishProductDeleted(productId);
        log.info("Product deleted: id={}", productId);
    }

    /* ---- Categories ---- */

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = request.getName().toLowerCase().replaceAll("\\s+", "-");

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .parentId(request.getParentId())
                .description(request.getDescription())
                .build();

        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryResponse).toList();
    }

    @Override
    public CategoryResponse getCategory(String categoryId) {
        return toCategoryResponse(findCategoryOrThrow(categoryId));
    }

    /* ---- Helpers ---- */

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Category findCategoryOrThrow(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .sellerId(p.getSellerId())
                .price(p.getPrice())
                .currency(p.getCurrency())
                .images(p.getImages())
                .attributes(p.getAttributes())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private CategoryResponse toCategoryResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .parentId(c.getParentId())
                .description(c.getDescription())
                .build();
    }
}
