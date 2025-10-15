package com.biobac.warehouse.service;

import com.biobac.warehouse.entity.AssetCategory;
import com.biobac.warehouse.entity.AssetStatus;
import com.biobac.warehouse.entity.DepreciationMethod;

import java.util.List;

public interface AssetInfoService {
    List<AssetCategory> getAssetCategories();
    List<AssetStatus> getAssetStatuses();
    List<DepreciationMethod> getAssetDepreciationMethods();
}
