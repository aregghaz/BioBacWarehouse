package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.WarehouseTypeRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import com.biobac.warehouse.service.WarehouseTypeService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/type")
@RequiredArgsConstructor
public class WarehouseTypeController {
    private final WarehouseTypeService warehouseTypeService;

    @GetMapping
    public ApiResponse<List<WarehouseTypeResponse>> get() {
        List<WarehouseTypeResponse> warehouseTypeResponses = warehouseTypeService.getAll();
        return ResponseUtil.success("Warehouse Types retrieved successfully", warehouseTypeResponses);
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseTypeResponse> getById(@PathVariable Long id) {
        WarehouseTypeResponse resp = warehouseTypeService.getById(id);
        return ResponseUtil.success("Warehouse Type retrieved successfully", resp);
    }

    @PostMapping("/all")
    public ApiResponse<List<WarehouseTypeResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                           @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<WarehouseTypeResponse>, PaginationMetadata> result = warehouseTypeService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Warehouse Types retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping
    public ApiResponse<WarehouseTypeResponse> create(@RequestBody WarehouseTypeRequest request) {
        WarehouseTypeResponse resp = warehouseTypeService.create(request);
        return ResponseUtil.success("Warehouse Type created successfully", resp);
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseTypeResponse> update(@PathVariable Long id, @RequestBody WarehouseTypeRequest request) {
        WarehouseTypeResponse resp = warehouseTypeService.update(id, request);
        return ResponseUtil.success("Warehouse Type updated successfully", resp);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        warehouseTypeService.delete(id);
        return ResponseUtil.success("Warehouse Type deleted successfully");
    }
}
