package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByProduct(Product product);
    List<InventoryItem> findByProductId(Long productId);
    List<InventoryItem> findByIngredient(Ingredient ingredient);
    List<InventoryItem> findByIngredientId(Long ingredientId);
    List<InventoryItem> findByWarehouseId(Long warehouseId);
    List<InventoryItem> findByGroupId(Long groupId);
}
