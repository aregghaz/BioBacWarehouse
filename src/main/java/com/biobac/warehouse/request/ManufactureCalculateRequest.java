package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManufactureCalculateRequest {
    private Long productId;
    private Double quantity;
}
