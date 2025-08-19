package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.WarehouseTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController extends BaseController {
    private final AuditLogService auditLogService;
    private final WarehouseService warehouseService;

    @PostMapping("/all")
    public ApiResponse<List<WarehouseTableResponse>> getAll(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestBody Map<String, FilterCriteria> filters
    ) {
        Pair<List<WarehouseTableResponse>, PaginationMetadata> result =
                warehouseService.getAll(filters, page, size, sortBy, sortDir);

        return ResponseUtil.success("Warehouses retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseDto> getById(@PathVariable Long id) {
        WarehouseDto warehouse = warehouseService.getById(id);
        return ResponseUtil.success("Warehouse retrieved successfully", warehouse);
    }

    @PostMapping
    public ApiResponse<WarehouseDto> create(@RequestBody WarehouseDto dto, HttpServletRequest request) {
        dto.setId(null); // new entity
        WarehouseDto createdWarehouse = warehouseService.create(dto);
        auditLogService.logCreate(Entities.WAREHOUSE.name(), createdWarehouse.getId(), dto, getUsername(request));
        return ResponseUtil.success("Warehouse created successfully", createdWarehouse);
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseDto> update(@PathVariable Long id, @RequestBody WarehouseDto dto, HttpServletRequest request) {
        WarehouseDto existingWarehouse = warehouseService.getById(id);
        WarehouseDto warehouseDto = warehouseService.update(id, dto);
        auditLogService.logUpdate(Entities.WAREHOUSE.name(), id, existingWarehouse, warehouseDto, getUsername(request));
        return ResponseUtil.success("Warehouse updated successfully", warehouseDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseUtil.success("Warehouse deleted successfully");
    }
}
