package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.request.WarehouseRequest;
import com.biobac.warehouse.response.WarehouseResponse;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import com.biobac.warehouse.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.biobac.warehouse.mapper.WarehouseTypeMapper;

import java.util.List;

@Component
public class WarehouseMapper {

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private WarehouseTypeMapper warehouseTypeMapper;

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
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedAt(warehouse.getUpdatedAt());
        try {
            if (warehouse.getId() != null) {
                response.setAttributes(attributeService.getValuesForWarehouse(warehouse.getId()));
            }
        } catch (Exception ignored) {
        }
        try {
            if (warehouse.getTypes() != null) {
                List<WarehouseTypeResponse> types = warehouse.getTypes().stream()
                        .map(warehouseTypeMapper::toResponse)
                        .toList();
                response.setTypes(types);
            }
        } catch (Exception ignored) {
        }
        return response;
    }
}
