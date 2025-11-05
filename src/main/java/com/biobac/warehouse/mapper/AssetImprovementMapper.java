package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.AssetImprovement;
import com.biobac.warehouse.request.AddImprovementRequest;
import com.biobac.warehouse.response.AssetImprovementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetImprovementMapper {
    @Mapping(target = "assetId", source = "asset.id")
    @Mapping(target = "action", source = "action")
    AssetImprovementResponse toResponse(AssetImprovement entity);

    AssetImprovement toEntity(AddImprovementRequest request);
}
