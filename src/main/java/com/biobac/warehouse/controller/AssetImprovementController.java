package com.biobac.warehouse.controller;

import com.biobac.warehouse.service.AssetImprovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asset-improvement")
@RequiredArgsConstructor
public class AssetImprovementController {
    private final AssetImprovementService assetImprovementService;
}
