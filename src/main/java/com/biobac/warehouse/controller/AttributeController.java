package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.AttributeDataType;
import com.biobac.warehouse.request.AttributeDefRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AttributeDefResponse;
import com.biobac.warehouse.service.AttributeService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;


    @PostMapping("/definitions/create")
    public ApiResponse<AttributeDefResponse> createAttributeInGroup(@RequestBody AttributeDefRequest request) {
        AttributeDefResponse created = attributeService.createAttributeDefinition(request);
        return ResponseUtil.success("Attribute created successfully", created);
    }

    @PutMapping("/definitions/update/{id}")
    public ApiResponse<AttributeDefResponse> updateAttribute(@PathVariable Long id, @RequestBody AttributeDefRequest request) {
        AttributeDefResponse updated = attributeService.updateAttributeDefinition(id, request);
        return ResponseUtil.success("Attribute updated successfully", updated);
    }

    @GetMapping("/definitions/{id}")
    public ApiResponse<AttributeDefResponse> getById(@PathVariable Long id) {
        AttributeDefResponse attributeDefResponse = attributeService.getById(id);
        return ResponseUtil.success("Attribute retrieved successfully", attributeDefResponse);
    }

    @DeleteMapping("/definitions/delete/{id}")
    public ApiResponse<String> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttributeDefinition(id);
        return ResponseUtil.success("Attribute deleted successfully");
    }

    @GetMapping("/definitions/by-groups")
    public ApiResponse<List<AttributeDefResponse>> getDefinitionsByGroups(@RequestParam(value = "groupIds", required = false) List<Long> groupIds) {
        List<AttributeDefResponse> defs = attributeService.getDefinitionsByGroups(groupIds);
        return ResponseUtil.success("Attribute definitions retrieved successfully", defs);
    }

    @PostMapping("/all")
    public ApiResponse<List<AttributeDefResponse>> getDefinitionsByGroupsPaged(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                               @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                               @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                               @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                               @RequestBody(required = false) Map<String, FilterCriteria> filters) {
        Pair<List<AttributeDefResponse>, PaginationMetadata> result = attributeService.getPagination(filters == null ? Map.of() : filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Attribute definitions retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/data-types")
    public ApiResponse<Set<AttributeDataType>> getAll() {
        Set<AttributeDataType> attributeDataTypes = attributeService.getAttributeDataTypes();
        return ResponseUtil.success("Attribute data types retrieved successfully", attributeDataTypes);
    }
}
