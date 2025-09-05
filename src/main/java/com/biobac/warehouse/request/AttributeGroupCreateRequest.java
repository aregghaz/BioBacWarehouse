package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeGroupCreateRequest {
    private String name;
    private String description;
    private List<Long> attributeIds;
}
