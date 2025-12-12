package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.UnitCreateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.UnitService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/unit")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @PostMapping
    public ApiResponse<UnitDto> create(@RequestBody UnitCreateRequest request) {
        UnitDto created = unitService.create(request);
        return ResponseUtil.success("Unit created successfully", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<UnitDto> getById(@PathVariable Long id) {
        UnitDto dto = unitService.getById(id);
        return ResponseUtil.success("Unit retrieved successfully", dto);
    }

    @GetMapping
    public ApiResponse<List<UnitDto>> getAll() {
        List<UnitDto> list = unitService.getAll();
        return ResponseUtil.success("Units retrieved successfully", list);
    }

    @PutMapping("/{id}")
    public ApiResponse<UnitDto> update(@PathVariable Long id, @RequestBody UnitCreateRequest request) {
        UnitDto dto = unitService.update(id, request);
        return ResponseUtil.success("Unit updated successfully", dto);
    }

    @PostMapping("/all")
    public ApiResponse<List<UnitDto>> getAllUnits(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                  @RequestParam(required = false, defaultValue = "10") Integer size,
                                                  @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                  @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                  @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<UnitDto>, PaginationMetadata> result = unitService.pagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Units retrieved successfully", result.getFirst(), result.getSecond());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseUtil.success("Unit deleted successfully");
    }
}
