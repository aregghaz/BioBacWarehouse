package com.biobac.warehouse.request;

import com.biobac.warehouse.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeDefRequest {
    private List<Long> attributeGroupIds;
    private String name;
    private AttributeDataType dataType;
}
