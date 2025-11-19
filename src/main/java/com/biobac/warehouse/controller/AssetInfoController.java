package com.biobac.warehouse.controller;

import com.biobac.warehouse.entity.AssetAction;
import com.biobac.warehouse.entity.AssetCategory;
import com.biobac.warehouse.entity.AssetStatus;
import com.biobac.warehouse.entity.DepreciationMethod;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.SelectResponse;
import com.biobac.warehouse.service.AssetInfoService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/asset-info")
@RequiredArgsConstructor
public class AssetInfoController {
    private final AssetInfoService assetInfoService;

    @GetMapping("/status")
    public ApiResponse<List<AssetStatus>> getStatuses() {
        List<AssetStatus> statuses = assetInfoService.getAssetStatuses();
        return ResponseUtil.success("Statuses retrieved successfully", statuses);
    }

    @GetMapping("/category")
    public ApiResponse<List<AssetCategory>> getCategories() {
        List<AssetCategory> statuses = assetInfoService.getAssetCategories();
        return ResponseUtil.success("Categories retrieved successfully", statuses);
    }

    @GetMapping("/depreciation-method")
    public ApiResponse<List<DepreciationMethod>> getDepreciationMethods() {
        List<DepreciationMethod> statuses = assetInfoService.getAssetDepreciationMethods();
        return ResponseUtil.success("Methods retrieved successfully", statuses);
    }

    @GetMapping("/action")
    public ApiResponse<List<AssetAction>> getActions() {
        List<AssetAction> actions = assetInfoService.getAssetActions();
        return ResponseUtil.success("Actions retrieved successfully", actions);
    }
}
