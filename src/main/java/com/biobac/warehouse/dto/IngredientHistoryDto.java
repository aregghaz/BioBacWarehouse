package com.biobac.warehouse.dto;

import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientHistoryDto {
    private Ingredient ingredient;
    private Warehouse warehouse;
    private Double quantityChange;
    private String notes;
    private BigDecimal lastPrice;
    private Long lastCompanyId;
    private LocalDateTime timestamp;
    private HistoryAction action;
}