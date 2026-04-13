package com.company.review_service.controller;

import com.company.review_service.document.Review;
import com.company.review_service.dto.request.ReviewRequest;
import com.company.review_service.dto.response.ApiResponse;
import com.company.review_service.dto.response.RatingSummaryResponse;
import com.company.review_service.service.ReviewService;
import com.company.review_service.utils.HeaderUtils;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews and ratings APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a review")
    public ResponseEntity<ApiResponse<Review>> submit(@Valid @RequestBody ReviewRequest request) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Review submitted", reviewService.submitReview(userId, request)));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<ApiResponse<Page<Review>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched",
                reviewService.getProductReviews(productId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get rating summary for a product")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getSummary(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", reviewService.getRatingSummary(productId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a review by ID")
    public ResponseEntity<ApiResponse<Review>> getReview(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Review fetched", reviewService.getReview(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update my review")
    public ResponseEntity<ApiResponse<Review>> update(@PathVariable String id, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review updated", reviewService.updateReview(id, HeaderUtils.getCurrentUserId(), request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete my review")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        reviewService.deleteReview(id, HeaderUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }

    @PostMapping("/{id}/helpful")
    @Operation(summary = "Mark review as helpful")
    public ResponseEntity<ApiResponse<Review>> markHelpful(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Marked helpful", reviewService.markHelpful(id)));
    }

    @GetMapping("/my")
    @Operation(summary = "My reviews")
    public ResponseEntity<ApiResponse<Page<Review>>> myReviews(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("My reviews", reviewService.getMyReviews(
                HeaderUtils.getCurrentUserId(), PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Approve/Reject review (ADMIN)")
    public ResponseEntity<ApiResponse<Review>> moderate(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Review moderated", reviewService.moderateReview(id, status)));
    }
}
