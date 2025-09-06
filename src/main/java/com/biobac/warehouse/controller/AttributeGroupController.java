package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AttributeGroupResponse;
import com.biobac.warehouse.service.AttributeGroupService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attribute-groups")
@RequiredArgsConstructor
public class AttributeGroupController extends BaseController {

    private final AttributeGroupService service;

    @GetMapping
    public ApiResponse<List<AttributeGroupResponse>> getAll() {
        List<AttributeGroupResponse> groups = service.getAll();
        return ResponseUtil.success("Attribute groups retrieved successfully", groups);
    }

    @PostMapping("/all")
    public ApiResponse<List<AttributeGroupResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                            @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                            @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                            @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<AttributeGroupResponse>, PaginationMetadata> result = service.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Attribute groups retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<AttributeGroupResponse> getById(@PathVariable Long id) {
        AttributeGroupResponse group = service.getById(id);
        return ResponseUtil.success("Attribute group retrieved successfully", group);
    }

    @PostMapping
    public ApiResponse<AttributeGroupResponse> create(@RequestBody AttributeGroupCreateRequest requestDto,
                                                      HttpServletRequest request) {
        AttributeGroupResponse created = service.create(requestDto);
        return ResponseUtil.success("Attribute group created successfully", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<AttributeGroupResponse> update(@PathVariable Long id,
                                                      @RequestBody AttributeGroupCreateRequest requestDto,
                                                      HttpServletRequest request) {
        AttributeGroupResponse updated = service.update(id, requestDto);
        return ResponseUtil.success("Attribute group updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id);
        return ResponseUtil.success("Attribute group deleted successfully");
    }
}
