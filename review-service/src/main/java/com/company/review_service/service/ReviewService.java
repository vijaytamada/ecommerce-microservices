package com.company.review_service.service;

import com.company.review_service.document.Review;
import com.company.review_service.dto.request.ReviewRequest;
import com.company.review_service.dto.response.RatingSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    Review submitReview(UUID userId, ReviewRequest request);
    Page<Review> getProductReviews(String productId, Pageable pageable);
    Review getReview(String reviewId);
    Review updateReview(String reviewId, UUID userId, ReviewRequest request);
    void deleteReview(String reviewId, UUID userId);
    Review markHelpful(String reviewId);
    Page<Review> getMyReviews(UUID userId, Pageable pageable);
    RatingSummaryResponse getRatingSummary(String productId);
    Review moderateReview(String reviewId, String status);
}
