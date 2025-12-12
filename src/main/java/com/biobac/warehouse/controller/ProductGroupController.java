package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductGroupDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ProductGroupResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.ProductGroupService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/product-groups")
@RequiredArgsConstructor
public class ProductGroupController extends BaseController {
    private final AuditLogService auditLogService;
    private final ProductGroupService service;

    @GetMapping
    public ApiResponse<List<ProductGroupResponse>> getAll() {
        List<ProductGroupResponse> productGroupDtos = service.getPagination();
        return ResponseUtil.success("Product groups retrieved successfully", productGroupDtos);
    }

    @PostMapping("/all")
    public ApiResponse<List<ProductGroupResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductGroupResponse>, PaginationMetadata> result = service.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Product groups retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductGroupResponse> getById(@PathVariable Long id) {
        ProductGroupResponse productGroup = service.getById(id);
        return ResponseUtil.success("Product group retrieved successfully", productGroup);
    }

    @PostMapping
    public ApiResponse<ProductGroupResponse> create(@RequestBody ProductGroupDto dto, HttpServletRequest request) {
        dto.setId(null);
        ProductGroupResponse createdGroup = service.create(dto);
        return ResponseUtil.success("Product group created successfully", createdGroup);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductGroupResponse> update(@PathVariable Long id, @RequestBody ProductGroupDto dto, HttpServletRequest request) {
        ProductGroupResponse updatedGroup = service.update(id, dto);
        return ResponseUtil.success("Product group updated successfully", updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id);
        return ResponseUtil.success("Product group deleted successfully");
    }
}
