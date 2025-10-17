package com.biobac.warehouse.service;

import com.biobac.warehouse.entity.Asset;
import com.biobac.warehouse.request.AssetRegisterRequest;

public interface AssetService {
    Asset register(AssetRegisterRequest request);
}
