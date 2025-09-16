package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryItemResponse {
    private Long id;
    private Double quantity;
    private String warehouseName;
    private Long warehouseId;
    private String productName;
    private String ingredientName;
    private LocalDate importDate;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
