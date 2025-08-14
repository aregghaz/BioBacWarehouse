package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping
    public ApiResponse<List<WarehouseDto>> getAll() {
        List<WarehouseDto> warehouses = warehouseService.getAll();
        return ResponseUtil.success("Warehouses retrieved successfully", warehouses);
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseDto> getById(@PathVariable Long id) {
        WarehouseDto warehouse = warehouseService.getById(id);
        return ResponseUtil.success("Warehouse retrieved successfully", warehouse);
    }

    @PostMapping
    public ApiResponse<WarehouseDto> create(@RequestBody WarehouseDto dto) {
        dto.setId(null); // new entity
        WarehouseDto createdWarehouse = warehouseService.create(dto);
        return ResponseUtil.success("Warehouse created successfully", createdWarehouse);
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseDto> update(@PathVariable Long id, @RequestBody WarehouseDto dto) {
        WarehouseDto warehouseDto = warehouseService.update(id, dto);
        return ResponseUtil.success("Warehouse updated successfully", warehouseDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseUtil.success("Warehouse deleted successfully");
    }
}
