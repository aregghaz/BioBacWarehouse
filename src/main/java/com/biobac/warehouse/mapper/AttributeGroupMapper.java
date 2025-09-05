package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.AttributeGroup;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.response.AttributeGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AttributeGroupMapper {
    AttributeGroupResponse toDto(AttributeGroup entity);
    AttributeGroup toEntity(AttributeGroupCreateRequest request);
    void update(@MappingTarget AttributeGroup entity, AttributeGroupCreateRequest request);
}