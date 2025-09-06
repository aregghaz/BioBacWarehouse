package com.biobac.warehouse.response;

import com.biobac.warehouse.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeDefResponse {
    private Long id;
    private String name;
    private AttributeDataType dataType;
}
