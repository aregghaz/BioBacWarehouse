package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductGroupDto;
import com.biobac.warehouse.entity.ProductGroup;
import com.biobac.warehouse.response.ProductGroupResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductGroupMapper {
    ProductGroupResponse toDto(ProductGroup entity);
    ProductGroupResponse toResponse(ProductGroup entity);
    ProductGroup toEntity(ProductGroupDto dto);
    ProductGroupResponse toTableResponse(ProductGroup entity);
}
