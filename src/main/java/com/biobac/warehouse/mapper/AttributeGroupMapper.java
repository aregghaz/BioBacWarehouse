package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.AttributeGroup;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.response.AttributeDefResponse;
import com.biobac.warehouse.response.AttributeGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AttributeGroupMapper {
    @Mapping(source = "definitions", target = "attributes")
    AttributeGroupResponse toDto(AttributeGroup entity);

    AttributeDefResponse toDto(AttributeDefinition entity);

    AttributeGroup toEntity(AttributeGroupCreateRequest request);

    void update(@MappingTarget AttributeGroup entity, AttributeGroupCreateRequest request);
}