package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.AttributeTargetType;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.request.WarehouseRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AttributeResponse;
import com.biobac.warehouse.response.WarehouseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WarehouseMapper {

    @Autowired
    private WarehouseTypeMapper warehouseTypeMapper;
    @Autowired
    private AttributeClient attributeClient;

    public WarehouseDto toDto(Warehouse warehouse) {
        if (warehouse == null) return null;
        WarehouseDto dto = new WarehouseDto();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setLocation(warehouse.getLocation());
        return dto;
    }

    public Warehouse toEntity(WarehouseDto dto) {
        if (dto == null) return null;
        Warehouse warehouse = new Warehouse();
        warehouse.setId(dto.getId());
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        return warehouse;
    }

    public Warehouse toEntity(WarehouseRequest request) {
        if (request == null) return null;
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        return warehouse;
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        if (warehouse == null) return null;
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setName(warehouse.getName());
        response.setAttributeGroupIds(warehouse.getAttributeGroupIds());
        response.setLocation(warehouse.getLocation());
        if (warehouse.getWarehouseGroup() != null) {
            response.setWarehouseGroupId(warehouse.getWarehouseGroup().getId());
            response.setWarehouseGroupName(warehouse.getWarehouseGroup().getName());
        }
        if (warehouse.getWarehouseType() != null) {
            response.setWarehouseTypeId(warehouse.getWarehouseType().getId());
            response.setWarehouseTypeName(warehouse.getWarehouseType().getType());
        }
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedAt(warehouse.getUpdatedAt());
        try {
            if (warehouse.getId() != null) {
                ApiResponse<List<AttributeResponse>> attributes = attributeClient.getValues(warehouse.getId(), AttributeTargetType.WAREHOUSE.name());
                response.setAttributes(attributes.getData());
            }
        } catch (Exception ignored) {
        }
        return response;
    }
}
