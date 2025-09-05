package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.request.WarehouseRequest;
import com.biobac.warehouse.response.WarehouseResponse;
import com.biobac.warehouse.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    @Autowired
    private AttributeService attributeService;

    public WarehouseDto toDto(Warehouse warehouse) {
        if (warehouse == null) return null;
        WarehouseDto dto = new WarehouseDto();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setLocation(warehouse.getLocation());
        dto.setType(warehouse.getType());
        // attributeGroupIds and attributes are request-side helpers; we don't infer them from entity here
        return dto;
    }

    public Warehouse toEntity(WarehouseDto dto) {
        if (dto == null) return null;
        Warehouse warehouse = new Warehouse();
        warehouse.setId(dto.getId());
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setType(dto.getType());
        return warehouse;
    }

    public Warehouse toEntity(WarehouseRequest request) {
        if (request == null) return null;
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setType(request.getType());
        return warehouse;
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        if (warehouse == null) return null;
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setName(warehouse.getName());
        response.setLocation(warehouse.getLocation());
        response.setType(warehouse.getType());
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedAt(warehouse.getUpdatedAt());
        return response;
    }
}
