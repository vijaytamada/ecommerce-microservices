package com.company.order_service.service;

import com.company.order_service.dto.request.PlaceOrderRequest;
import com.company.order_service.dto.response.OrderResponse;
import com.company.order_service.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderResponse placeOrder(UUID userId, String userEmail, PlaceOrderRequest request);
    OrderResponse getOrder(UUID orderId);
    Page<OrderResponse> getMyOrders(UUID userId, Pageable pageable);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateStatus(UUID orderId, OrderStatus status);
    OrderResponse cancelOrder(UUID orderId, UUID userId);
}
