package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.DepartmentRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.DepartmentResponse;
import com.biobac.warehouse.service.DepartmentService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> getById(@PathVariable Long id) {
        DepartmentResponse response = departmentService.getById(id);
        return ResponseUtil.success("Department retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> getAll() {
        List<DepartmentResponse> responses = departmentService.getAll();
        return ResponseUtil.success("Departments retrieved successfully", responses);
    }

    @PostMapping
    public ApiResponse<DepartmentResponse> create(DepartmentRequest request) {
        DepartmentResponse response = departmentService.create(request);
        return ResponseUtil.success("Department created successfully", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id, DepartmentRequest request) {
        DepartmentResponse response = departmentService.update(id, request);
        return ResponseUtil.success("Department updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseUtil.success("Department deleted successfully");
    }

    @PostMapping("/all")
    public ApiResponse<List<DepartmentResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                        @RequestParam(required = false, defaultValue = "10") Integer size,
                                                        @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                        @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                        @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<DepartmentResponse>, PaginationMetadata> result = departmentService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Departments retrieved successfully", result.getFirst(), result.getSecond());
    }
}