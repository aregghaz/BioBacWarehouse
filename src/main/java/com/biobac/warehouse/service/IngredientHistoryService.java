package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.entity.Ingredient;

import java.time.LocalDateTime;
import java.util.List;

public interface IngredientHistoryService {
    
    /**
     * Record a change in ingredient quantity
     * 
     * @param ingredient The ingredient that changed
     * @param quantityBefore The quantity before the change
     * @param quantityAfter The quantity after the change
     * @param action The action that caused the change (e.g., "CREATED", "UPDATED", "USED_IN_RECIPE")
     * @param notes Additional notes about the change
     * @return The created history record as DTO
     */
    IngredientHistoryDto recordQuantityChange(Ingredient ingredient, Double quantityBefore, 
                                             Double quantityAfter, String action, String notes);
    
    /**
     * Get history for a specific ingredient
     * 
     * @param ingredientId The ID of the ingredient
     * @return List of history records for the ingredient
     */
    List<IngredientHistoryDto> getHistoryForIngredient(Long ingredientId);
    
    /**
     * Get history for a date range
     * 
     * @param startDate The start date/time
     * @param endDate The end date/time
     * @return List of history records within the date range
     */
    List<IngredientHistoryDto> getHistoryForDateRange(LocalDateTime startDate, LocalDateTime endDate);
}