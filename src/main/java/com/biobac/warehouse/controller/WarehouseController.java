package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.WarehouseRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.WarehouseResponse;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/warehouses")
@RequiredArgsConstructor
public class WarehouseController extends BaseController {
    private final WarehouseService warehouseService;

    @PostMapping("/all")
    public ApiResponse<List<WarehouseResponse>> getAll(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestBody Map<String, FilterCriteria> filters
    ) {
        Pair<List<WarehouseResponse>, PaginationMetadata> result =
                warehouseService.getPagination(filters, page, size, sortBy, sortDir);

        return ResponseUtil.success("Warehouses retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> getAll() {
        List<WarehouseResponse> warehouses = warehouseService.getAll();
        return ResponseUtil.success("Warehouses retrieved successfully", warehouses);
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> getById(@PathVariable Long id) {
        WarehouseResponse warehouse = warehouseService.getById(id);
        return ResponseUtil.success("Warehouse retrieved successfully", warehouse);
    }

    @PostMapping
    public ApiResponse<WarehouseResponse> create(@RequestBody WarehouseRequest dto, HttpServletRequest request) {
        WarehouseResponse createdWarehouse = warehouseService.create(dto);
        return ResponseUtil.success("Warehouse created successfully", createdWarehouse);
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> update(@PathVariable Long id, @RequestBody WarehouseRequest dto, HttpServletRequest request) {
        WarehouseResponse warehouseDto = warehouseService.update(id, dto);
        return ResponseUtil.success("Warehouse updated successfully", warehouseDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        warehouseService.delete(id);
        return ResponseUtil.success("Warehouse deleted successfully");
    }
}
