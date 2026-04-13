package com.company.review_service.service.impl;

import com.company.review_service.config.RabbitMQConfig;
import com.company.review_service.document.Review;
import com.company.review_service.dto.request.ReviewRequest;
import com.company.review_service.dto.response.RatingSummaryResponse;
import com.company.review_service.exception.ResourceNotFoundException;
import com.company.review_service.repository.ReviewRepository;
import com.company.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Review submitReview(UUID userId, ReviewRequest request) {
        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(userId)
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .status("APPROVED")
                .build();

        review = reviewRepository.save(review);

        // Notify search-service to update product rating
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "REVIEW_SUBMITTED");
        event.put("productId", request.getProductId());
        event.put("reviewId", review.getId());
        event.put("rating", request.getRating());
        event.put("timestamp", Instant.now().toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.REVIEW_EXCHANGE, RabbitMQConfig.REVIEW_SUBMITTED_KEY, event);

        return review;
    }

    @Override
    public Page<Review> getProductReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, "APPROVED", pageable);
    }

    @Override
    public Review getReview(String reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

    @Override
    public Review updateReview(String reviewId, UUID userId, ReviewRequest request) {
        Review review = getReview(reviewId);
        if (!review.getUserId().equals(userId)) throw new IllegalStateException("Not your review");
        if (request.getRating() != 0) review.setRating(request.getRating());
        if (request.getTitle() != null) review.setTitle(request.getTitle());
        if (request.getBody() != null) review.setBody(request.getBody());
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(String reviewId, UUID userId) {
        Review review = getReview(reviewId);
        if (!review.getUserId().equals(userId)) throw new IllegalStateException("Not your review");
        reviewRepository.delete(review);
    }

    @Override
    public Review markHelpful(String reviewId) {
        Review review = getReview(reviewId);
        review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        return reviewRepository.save(review);
    }

    @Override
    public Page<Review> getMyReviews(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    @Override
    public RatingSummaryResponse getRatingSummary(String productId) {
        List<Review> reviews = reviewRepository.findAll().stream()
                .filter(r -> r.getProductId().equals(productId) && "APPROVED".equals(r.getStatus()))
                .toList();

        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            breakdown.put(star, reviews.stream().filter(r -> r.getRating() == star).count());
        }

        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        return RatingSummaryResponse.builder()
                .productId(productId)
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(reviews.size())
                .ratingBreakdown(breakdown)
                .build();
    }

    @Override
    public Review moderateReview(String reviewId, String status) {
        Review review = getReview(reviewId);
        review.setStatus(status);
        return reviewRepository.save(review);
    }
}
