package com.biobac.warehouse.response;

import com.biobac.warehouse.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttributeResponse {
    private Long id;
    private String name;
    private AttributeDataType dataType;
    private Object value;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
