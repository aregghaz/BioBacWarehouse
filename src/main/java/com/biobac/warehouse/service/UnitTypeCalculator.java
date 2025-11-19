package com.biobac.warehouse.service;

import com.biobac.warehouse.request.InventoryUnitTypeRequest;
import com.biobac.warehouse.response.UnitTypeCalculatedResponse;

import java.util.List;

public interface UnitTypeCalculator {
    List<UnitTypeCalculatedResponse> calculateUnitTypes(Long id, InventoryUnitTypeRequest request);
}

