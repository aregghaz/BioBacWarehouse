package com.biobac.warehouse.response;

import com.biobac.warehouse.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AttributeDefResponse {
    private Long id;
    private String name;
    private AttributeDataType dataType;
    private List<Long> attributeGroupIds;
    private List<OptionValueResponse> options;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
