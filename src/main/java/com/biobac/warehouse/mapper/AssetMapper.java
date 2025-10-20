package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.Asset;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.response.AssetResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "depreciationMethodId", source = "depreciationMethod.id")
    @Mapping(target = "depreciationMethodName", source = "depreciationMethod.name")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    AssetResponse toResponse(Asset entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "depreciationMethod", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    Asset toEntity(AssetRegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "depreciationMethod", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    void updateEntityFromRequest(AssetRegisterRequest request, @MappingTarget Asset asset);
}
