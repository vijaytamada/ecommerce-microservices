package com.company.inventory_service.service;

import com.company.inventory_service.dto.request.BulkCheckRequest;
import com.company.inventory_service.dto.request.ReserveRequest;
import com.company.inventory_service.dto.request.StockRequest;
import com.company.inventory_service.dto.response.BulkAvailabilityResponse;
import com.company.inventory_service.dto.response.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryResponse createStock(StockRequest request);
    InventoryResponse getStock(String productId);
    InventoryResponse updateStock(String productId, int quantity);
    List<InventoryResponse> getLowStockItems();
    BulkAvailabilityResponse bulkCheck(BulkCheckRequest request);
    void reserve(ReserveRequest request);
    void confirmReservation(UUID orderId);
    void releaseReservation(UUID orderId);
}
