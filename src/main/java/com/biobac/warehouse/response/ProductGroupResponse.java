package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProductGroupResponse {
    private Long id;
    private String name;
    private List<AttributeDefResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
