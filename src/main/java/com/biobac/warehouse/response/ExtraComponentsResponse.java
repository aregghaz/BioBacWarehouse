package com.biobac.warehouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExtraComponentsResponse {
    private Long ingredientId;
    private Long productId;
    private String name;
    private List<UnitTypeCalculatedResponse> unitTypeConfigs;
}
