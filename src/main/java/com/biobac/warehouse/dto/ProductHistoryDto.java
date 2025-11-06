package com.biobac.warehouse.dto;

import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.entity.Product;
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
public class ProductHistoryDto {
    private Product product;
    private Warehouse warehouse;
    private Double quantityResult;
    private Double quantityChange;
    private String notes;
    private BigDecimal lastPrice;
    private Long companyId;
    private LocalDateTime timestamp;
    private HistoryAction action;
}