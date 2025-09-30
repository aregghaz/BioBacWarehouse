package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitTypeDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryUnitTypeRequest;
import com.biobac.warehouse.request.UnitTypeCreateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.UnitTypeCalculatedResponse;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.service.UnitTypeCalculator;
import com.biobac.warehouse.service.UnitTypeService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/unit-type")
@RequiredArgsConstructor
public class UnitTypeController {
    private final UnitTypeService unitTypeService;
    private final ProductService productService;
    private final IngredientService ingredientService;

    @PostMapping
    public ApiResponse<UnitTypeDto> create(@RequestBody UnitTypeCreateRequest request) {
        UnitTypeDto created = unitTypeService.create(request);
        return ResponseUtil.success("Unit type created successfully", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<UnitTypeDto> getById(@PathVariable Long id) {
        UnitTypeDto dto = unitTypeService.getById(id);
        return ResponseUtil.success("Unit type retrieved successfully", dto);
    }

    @GetMapping
    public ApiResponse<List<UnitTypeDto>> getAll() {
        List<UnitTypeDto> list = unitTypeService.getAll();
        return ResponseUtil.success("Unit types retrieved successfully", list);
    }

    @PostMapping("/all")
    public ApiResponse<List<UnitTypeDto>> getAllUnitTypes(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<UnitTypeDto>, PaginationMetadata> result = unitTypeService.pagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Unit types retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PutMapping("/{id}")
    public ApiResponse<UnitTypeDto> update(@PathVariable Long id, @RequestBody UnitTypeCreateRequest request) {
        UnitTypeDto updated = unitTypeService.update(id, request);
        return ResponseUtil.success("Unit type updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        unitTypeService.delete(id);
        return ResponseUtil.success("Unit type deleted successfully");
    }

    @PostMapping("/calculation/{type}/{id}")
    public ApiResponse<List<UnitTypeCalculatedResponse>> calculate(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestBody InventoryUnitTypeRequest request
    ) {
        UnitTypeCalculator calculator = switch (type.toLowerCase()) {
            case "product" -> productService;
            case "ingredient" -> ingredientService;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };

        List<UnitTypeCalculatedResponse> responses = calculator.calculateUnitTypes(id, request);
        return ResponseUtil.success("Unit types calculated", responses);
    }
}
