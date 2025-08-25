package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.WarehouseResponse;
import com.biobac.warehouse.response.WarehouseTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController extends BaseController {
    private final AuditLogService auditLogService;
    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    @PostMapping("/all")
    public ApiResponse<List<WarehouseTableResponse>> getAll(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestBody Map<String, FilterCriteria> filters
    ) {
        Pair<List<WarehouseTableResponse>, PaginationMetadata> result =
                warehouseService.getPagination(filters, page, size, sortBy, sortDir);

        return ResponseUtil.success("Warehouses retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> getAll() {
        List<WarehouseResponse> warehouses = warehouseService.getAll().stream()
                .map(dto -> warehouseMapper.toResponse(warehouseMapper.toEntity(dto)))
                .collect(Collectors.toList());
        return ResponseUtil.success("Warehouses retrieved successfully", warehouses);
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> getById(@PathVariable Long id) {
        WarehouseDto warehouse = warehouseService.getById(id);
        return ResponseUtil.success("Warehouse retrieved successfully", warehouseMapper.toResponse(warehouseMapper.toEntity(warehouse)));
    }

    @PostMapping
    public ApiResponse<WarehouseResponse> create(@RequestBody WarehouseDto dto, HttpServletRequest request) {
        dto.setId(null); // new entity
        WarehouseDto createdWarehouse = warehouseService.create(dto);
        auditLogService.logCreate(Entities.WAREHOUSE.name(), createdWarehouse.getId(), dto, getUsername(request));
        return ResponseUtil.success("Warehouse created successfully", warehouseMapper.toResponse(warehouseMapper.toEntity(createdWarehouse)));
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> update(@PathVariable Long id, @RequestBody WarehouseDto dto, HttpServletRequest request) {
        WarehouseDto existingWarehouse = warehouseService.getById(id);
        WarehouseDto warehouseDto = warehouseService.update(id, dto);
        auditLogService.logUpdate(Entities.WAREHOUSE.name(), id, existingWarehouse, warehouseDto, getUsername(request));
        return ResponseUtil.success("Warehouse updated successfully", warehouseMapper.toResponse(warehouseMapper.toEntity(warehouseDto)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        warehouseService.delete(id);
        auditLogService.logDelete(Entities.WAREHOUSE.name(), id, getUsername(request));
        return ResponseUtil.success("Warehouse deleted successfully");
    }
}
