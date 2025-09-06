package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AttributeValueResponse extends AttributeDefResponse {
    private String value;
    private Object parsedValue;
}
