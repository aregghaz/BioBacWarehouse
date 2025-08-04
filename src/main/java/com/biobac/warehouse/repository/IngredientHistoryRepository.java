package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IngredientHistoryRepository extends JpaRepository<IngredientHistory, Long> {
    
    List<IngredientHistory> findByIngredientOrderByTimestampDesc(Ingredient ingredient);
    
    List<IngredientHistory> findByIngredientIdOrderByTimestampDesc(Long ingredientId);
    
    List<IngredientHistory> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
}