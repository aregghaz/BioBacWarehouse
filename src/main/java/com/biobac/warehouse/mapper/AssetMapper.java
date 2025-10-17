package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.Asset;
import com.biobac.warehouse.response.AssetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetResponse toResponse(Asset entity);
}
