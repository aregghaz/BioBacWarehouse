package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.mapper.WarehouseTypeMapper;
import com.biobac.warehouse.repository.WarehouseTypeRepository;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import com.biobac.warehouse.service.WarehouseTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseTypeServiceImpl implements WarehouseTypeService {
    private final WarehouseTypeMapper warehouseTypeMapper;
    private final WarehouseTypeRepository warehouseTypeRepository;

    @Override
    public List<WarehouseTypeResponse> getAll() {
        return warehouseTypeRepository
                .findAll()
                .stream()
                .map(warehouseTypeMapper::toResponse).toList();
    }
}
