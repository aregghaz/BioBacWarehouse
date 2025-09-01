package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long>, JpaSpecificationExecutor<InventoryItem> {
    Optional<InventoryItem> findByWarehouseIdAndProductId(Long warehouse_id, Long product_id);

    Optional<InventoryItem> findByWarehouseIdAndIngredientId(Long warehouse_id, Long ingredient_id);
}
