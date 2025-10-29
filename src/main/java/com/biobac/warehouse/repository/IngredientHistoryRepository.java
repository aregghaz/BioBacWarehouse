package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

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
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId AND timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC, id DESC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findLastInRange(@Param("ingredientId") Long ingredientId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(
            value = "SELECT * FROM ingredient_history WHERE ingredient_id = :ingredientId AND timestamp < :start ORDER BY timestamp DESC, id DESC LIMIT 1",
            nativeQuery = true
    )
    IngredientHistory findFirstBeforeRange(
            @Param("ingredientId") Long ingredientId,
            @Param("start") LocalDate start
    );
}