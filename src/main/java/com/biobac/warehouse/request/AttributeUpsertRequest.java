package com.biobac.warehouse.request;

import com.biobac.warehouse.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AttributeUpsertRequest {
    private String name;
    private AttributeDataType dataType;
    private String value;
}
