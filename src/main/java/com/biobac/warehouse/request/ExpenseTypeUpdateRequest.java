package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseTypeUpdateRequest {
    private Long id;
    private String name;
}
