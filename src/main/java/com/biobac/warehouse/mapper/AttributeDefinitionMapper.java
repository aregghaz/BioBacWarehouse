package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.response.AttributeDefResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {
    AttributeDefResponse toDto(AttributeDefinition entity);
}
