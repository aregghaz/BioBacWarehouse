package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseTypeResponse extends AuditableResponse {
    private Long Id;
    private String name;
}
