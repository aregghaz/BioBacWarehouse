package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByProduct(Product product);
    List<InventoryItem> findByProductId(Long productId);
    List<InventoryItem> findByIngredient(Ingredient ingredient);
    List<InventoryItem> findByIngredientId(Long ingredientId);
    List<InventoryItem> findByWarehouseId(Long warehouseId);
    List<InventoryItem> findByGroupId(Long groupId);
    
    // Custom query to find inventory items by ingredient ID and warehouse ID
    List<InventoryItem> findByIngredientIdAndWarehouseId(Long ingredientId, Long warehouseId);
    
    // Custom query to update ingredient count
    @Modifying
    @Transactional
    @Query("UPDATE InventoryItem i SET i.ingredientCount = i.ingredientCount + 1 WHERE i.ingredient.id = :ingredientId AND i.warehouseId = :warehouseId")
    int incrementIngredientCount(@Param("ingredientId") Long ingredientId, @Param("warehouseId") Long warehouseId);
    
    // Custom query to set ingredient count directly
    @Modifying
    @Transactional
    @Query("UPDATE InventoryItem i SET i.ingredientCount = :count WHERE i.ingredient.id = :ingredientId AND i.warehouseId = :warehouseId")
    int setIngredientCount(@Param("ingredientId") Long ingredientId, @Param("warehouseId") Long warehouseId, @Param("count") Integer count);
    
    // Custom query to decrement ingredient quantity
    @Modifying
    @Transactional
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity - :amount WHERE i.ingredient.id = :ingredientId AND i.warehouseId = :warehouseId AND i.quantity >= :amount")
    int decrementIngredientQuantity(@Param("ingredientId") Long ingredientId, @Param("warehouseId") Long warehouseId, @Param("amount") int amount);
}
