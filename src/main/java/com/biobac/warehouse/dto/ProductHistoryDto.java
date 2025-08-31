package com.biobac.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistoryDto {
    private Long id;
    private Long productId;
    private String productName;
    private LocalDateTime timestamp;
    private String action;
    private Double quantityBefore;
    private Double quantityAfter;
    private String notes;
}