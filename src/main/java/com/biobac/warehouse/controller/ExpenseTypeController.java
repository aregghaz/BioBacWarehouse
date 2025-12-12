package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.ExpenseTypeCreateRequest;
import com.biobac.warehouse.request.ExpenseTypeUpdateRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ExpenseTypeResponse;
import com.biobac.warehouse.service.ExpenseTypeService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/expense-types")
@RequiredArgsConstructor
public class ExpenseTypeController {
    private final ExpenseTypeService service;

    @GetMapping
    public ApiResponse<List<ExpenseTypeResponse>> getAll() {
        List<ExpenseTypeResponse> list = service.getAll();
        return ResponseUtil.success("Expense types retrieved successfully", list);
    }

    @PostMapping("/all")
    public ApiResponse<List<ExpenseTypeResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                         @RequestParam(required = false, defaultValue = "10") Integer size,
                                                         @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                         @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                         @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ExpenseTypeResponse>, PaginationMetadata> result = service.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Expense types retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpenseTypeResponse> getById(@PathVariable Long id) {
        ExpenseTypeResponse resp = service.getById(id);
        return ResponseUtil.success("Expense type retrieved successfully", resp);
    }

    @PostMapping
    public ApiResponse<ExpenseTypeResponse> create(@RequestBody ExpenseTypeCreateRequest request) {
        ExpenseTypeResponse resp = service.create(request);
        return ResponseUtil.success("Expense type created successfully", resp);
    }

    @PutMapping("/{id}")
    public ApiResponse<ExpenseTypeResponse> update(@PathVariable Long id, @RequestBody ExpenseTypeUpdateRequest request) {
        request.setId(id);
        ExpenseTypeResponse resp = service.update(request);
        return ResponseUtil.success("Expense type updated successfully", resp);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseUtil.success("Expense type deleted successfully");
    }
}
