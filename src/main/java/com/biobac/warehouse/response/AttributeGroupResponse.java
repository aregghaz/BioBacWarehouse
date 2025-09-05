package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeGroupResponse {
    private Long id;
    private String name;
    private String description;
    private List<AttributeDefResponse> attributeDefinitions;
}
