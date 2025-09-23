package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long>, JpaSpecificationExecutor<InventoryItem> {
    List<InventoryItem> findByProductIdIn(List<Long> productIds);

    List<InventoryItem> findByIngredientIdIn(List<Long> ingredientIds);
}
