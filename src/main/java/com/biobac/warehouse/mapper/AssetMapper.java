package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.response.AssetResponse;
import com.biobac.warehouse.response.EntityReferenceResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    @Mapping(target = "category", source = "category")
    @Mapping(target = "depreciationMethod", source = "depreciationMethod")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "warehouse", source = "warehouse")
    AssetResponse toResponse(Asset entity);

    default EntityReferenceResponse mapCategory(AssetCategory entity) {
        if (entity == null) return null;
        EntityReferenceResponse dto = new EntityReferenceResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    default EntityReferenceResponse mapDepreciationMethod(DepreciationMethod entity) {
        if (entity == null) return null;
        EntityReferenceResponse dto = new EntityReferenceResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    default EntityReferenceResponse mapDepartment(Department entity) {
        if (entity == null) return null;
        EntityReferenceResponse dto = new EntityReferenceResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    default EntityReferenceResponse mapWarehouse(Warehouse entity) {
        if (entity == null) return null;
        EntityReferenceResponse dto = new EntityReferenceResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

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
