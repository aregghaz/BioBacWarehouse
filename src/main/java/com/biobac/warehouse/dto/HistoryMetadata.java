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
    private String unitName;
}
