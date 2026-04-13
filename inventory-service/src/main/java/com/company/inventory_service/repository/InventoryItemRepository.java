package com.company.inventory_service.repository;

import com.company.inventory_service.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findByQuantityAvailableLessThanEqual(int threshold);
}
