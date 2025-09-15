package com.biobac.warehouse.service;

import com.biobac.warehouse.response.WarehouseTypeResponse;

import java.util.List;

public interface WarehouseTypeService {
    List<WarehouseTypeResponse> getAll();
}
