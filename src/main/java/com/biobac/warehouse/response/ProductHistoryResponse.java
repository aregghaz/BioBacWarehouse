package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductHistoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productGroupName;
    private String unitName;
    private Double initialCount;
    private Double eventualCount;
    private Double increasedCount;
    private Double decreasedCount;
}
