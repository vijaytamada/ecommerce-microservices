package com.company.search_service.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Double)
    @Builder.Default
    private double averageRating = 0.0;

    @Field(type = FieldType.Long)
    @Builder.Default
    private long totalReviews = 0;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date)
    private LocalDateTime indexedAt;
}
