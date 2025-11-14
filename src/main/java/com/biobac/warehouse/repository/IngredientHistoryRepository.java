package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IngredientHistoryRepository extends JpaRepository<IngredientHistory, Long>, JpaSpecificationExecutor<IngredientHistory> {
    @Query(
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId ORDER BY timestamp ASC, id ASC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findEarliestByIngredientId(@Param("ingredientId") Long ingredientId);


    @Query(
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId ORDER BY timestamp DESC, id DESC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findLatestByIngredientId(@Param("ingredientId") Long ingredientId);

    @Query(
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId AND timestamp <= :end ORDER BY timestamp DESC, id DESC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findLastInRange(@Param("ingredientId") Long ingredientId, @Param("end") LocalDateTime end);

    @Query(
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId AND timestamp <= :end AND warehouse_id = :warehouseId ORDER BY timestamp DESC, id DESC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findLastInRangeWithWarehouseId(@Param("ingredientId") Long ingredientId, @Param("warehouseId") Long warehouseId, @Param("end") LocalDateTime end);

    @Query(
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId AND timestamp < :start ORDER BY timestamp DESC, id DESC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findFirstBeforeRange(
            @Param("ingredientId") Long ingredientId,
            @Param("start") LocalDateTime start
    );

    @Query(value = """
            select sum(ih.quantity_change) as sum
            from ingredient_history ih
                     join ingredient i on i.id = ih.ingredient_id
            where ih.increase = true
              and ih.timestamp between :from and :to
              and i.id = :ingredient_id
            """, nativeQuery = true)
    Double sumIncreasedCount(@Param("ingredient_id") Long ingredientId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    @Query(value = """
            select sum(ih.quantity_change) as sum
            from ingredient_history ih
                     join ingredient i on i.id = ih.ingredient_id
            where ih.increase = false
              and ih.timestamp between :from and :to
              and i.id = :ingredient_id
            """, nativeQuery = true)
    Double sumDecreasedCount(@Param("ingredient_id") Long ingredientId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    Optional<IngredientHistory> findFirstByWarehouseAndIngredientOrderByTimestampDescIdDesc(Warehouse warehouse, Ingredient ingredient);
}