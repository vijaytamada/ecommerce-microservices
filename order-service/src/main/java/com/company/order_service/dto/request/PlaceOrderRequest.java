package com.company.order_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaceOrderRequest {

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address is required")
    private ShippingAddressRequest shippingAddress;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemRequest {
        @NotNull private String productId;
        @NotNull private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ShippingAddressRequest {
        private String fullName;
        private String street;
        private String city;
        private String state;
        private String country;
        private String zipCode;
        private String phone;
    }
}
