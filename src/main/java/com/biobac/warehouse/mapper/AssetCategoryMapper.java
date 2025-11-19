package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.AssetCategory;
import com.biobac.warehouse.request.AssetCategoryRequest;
import com.biobac.warehouse.response.AssetCategoryResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AssetCategoryMapper {
    AssetCategoryResponse toResponse(AssetCategory entity);

    AssetCategory toEntity(AssetCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AssetCategoryRequest request, @MappingTarget AssetCategory entity);

}
