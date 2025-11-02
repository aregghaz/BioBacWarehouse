package com.biobac.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryMetadata {
    private PaginationMetadata pagination;
    private Double total;
    private Double initial;
    private Double eventual;
    private Double increase;
    private Double decrease;
    private String unitName;
    private String ingredientName;
}
