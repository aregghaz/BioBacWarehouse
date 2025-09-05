package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseGroupDto;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.WarehouseGroupResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.WarehouseGroupService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse-groups")
@RequiredArgsConstructor
public class WarehouseGroupController extends BaseController {
    private final AuditLogService auditLogService;
    private final WarehouseGroupService service;

    @GetMapping
    public ApiResponse<List<WarehouseGroupResponse>> getAll() {
        List<WarehouseGroupResponse> dtos = service.getPagination();
        return ResponseUtil.success("Warehouse groups retrieved successfully", dtos);
    }

    @PostMapping("/all")
    public ApiResponse<List<WarehouseGroupResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                            @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                            @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                            @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<WarehouseGroupResponse>, PaginationMetadata> result = service.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Warehouse groups retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseGroupResponse> getById(@PathVariable Long id) {
        WarehouseGroupResponse group = service.getById(id);
        return ResponseUtil.success("Warehouse group retrieved successfully", group);
    }

    @PostMapping
    public ApiResponse<WarehouseGroupResponse> create(@RequestBody WarehouseGroupDto dto, HttpServletRequest request) {
        dto.setId(null);
        WarehouseGroupResponse createdGroup = service.create(dto);
        auditLogService.logCreate(Entities.WAREHOUSEGROUP.name(), createdGroup.getId(), dto, getUsername(request));
        return ResponseUtil.success("Warehouse group created successfully", createdGroup);
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseGroupResponse> update(@PathVariable Long id, @RequestBody WarehouseGroupDto dto, HttpServletRequest request) {
        WarehouseGroupResponse existingGroup = service.getById(id);
        WarehouseGroupResponse updatedGroup = service.update(id, dto);
        auditLogService.logUpdate(Entities.WAREHOUSEGROUP.name(), updatedGroup.getId(), existingGroup, dto, getUsername(request));
        return ResponseUtil.success("Warehouse group updated successfully", updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id);
        auditLogService.logDelete(Entities.WAREHOUSEGROUP.name(), id, getUsername(request));
        return ResponseUtil.success("Warehouse group deleted successfully");
    }
}
