package com.biobac.warehouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExtraComponentsResponse {
    private Long ingredientId;
    private Long productId;
}
