package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ManufactureCalculateMetadata {
    private Long productId;
    private String productName;
    private Double quantity;
    private BigDecimal price;
    private BigDecimal amount;
}
