package com.company.review_service.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    private String id;

    @Indexed
    private String productId;

    @Indexed
    private UUID userId;

    private UUID orderId;

    private int rating;         // 1-5

    private String title;

    private String body;

    @Builder.Default
    private String status = "APPROVED";   // PENDING, APPROVED, REJECTED

    @Builder.Default
    private int helpfulVotes = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
