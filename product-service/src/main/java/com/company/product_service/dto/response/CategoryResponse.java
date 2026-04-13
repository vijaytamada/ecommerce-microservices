package com.company.product_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private String id;
    private String name;
    private String slug;
    private String parentId;
    private String description;
}
