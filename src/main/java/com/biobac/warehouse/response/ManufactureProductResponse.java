package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ManufactureProductResponse extends AuditableResponse {
    private Long id;
    private Double quantity;
    private String warehouseName;
    private Long warehouseId;
    private String unitName;
    private String productName;
    private Long productId;
    private LocalDateTime expirationDate;
    private LocalDateTime manufacturingDate;
    private BigDecimal price;
}
