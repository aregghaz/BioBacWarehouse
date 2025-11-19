package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AddImprovementRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AssetImprovementResponse;
import com.biobac.warehouse.service.AssetImprovementService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset-improvement")
@RequiredArgsConstructor
public class AssetImprovementController {
    private final AssetImprovementService assetImprovementService;

    @PostMapping("/{assetId}")
    public AssetImprovementResponse addImprovement(@PathVariable Long assetId, @RequestBody AddImprovementRequest request) {
        return assetImprovementService.addImprovement(assetId, request);
    }

    @PostMapping("/all/{assetId}")
    public ApiResponse<List<AssetImprovementResponse>> getImprovements(@PathVariable Long assetId,
                                                                       @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                       @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                       @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                       @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                       @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<AssetImprovementResponse>, PaginationMetadata> result = assetImprovementService.getPaginationByAssetId(assetId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Asset Improvements retrieved successfully", result.getFirst(), result.getSecond());
    }
}
