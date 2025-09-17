package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeUpsertRequest {
    private Long id;
    private Object value;
}
