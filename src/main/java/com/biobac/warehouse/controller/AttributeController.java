package com.biobac.warehouse.controller;

import com.biobac.warehouse.entity.AttributeDataType;
import com.biobac.warehouse.request.AttributeDefRequest;
import com.biobac.warehouse.request.AttributeUpsertRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AttributeDefResponse;
import com.biobac.warehouse.service.AttributeService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/definitions/by-groups")
    public ApiResponse<List<AttributeDefResponse>> getDefinitionsByGroups(@RequestParam("groupIds") List<Long> groupIds) {
        List<AttributeDefResponse> defs = attributeService.getDefinitionsByGroups(groupIds);
        return ResponseUtil.success("Attribute definitions retrieved successfully", defs);
    }

    @GetMapping("/data-types")
    public ApiResponse<Set<AttributeDataType>> getAll() {
        Set<AttributeDataType> attributeDataTypes = attributeService.getAttributeDataTypes();
        return ResponseUtil.success("Attribute data types retrieved successfully", attributeDataTypes);
    }
}
