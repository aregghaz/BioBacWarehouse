package com.biobac.warehouse.controller;

import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import com.biobac.warehouse.service.WarehouseTypeService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse/type")
@RequiredArgsConstructor
public class WarehouseTypeController {
    private final WarehouseTypeService warehouseTypeService;

    @GetMapping
    public ApiResponse<List<WarehouseTypeResponse>> get() {
        List<WarehouseTypeResponse> warehouseTypeResponses = warehouseTypeService.getAll();
        return ResponseUtil.success("Warehouse Types retrieved successfully", warehouseTypeResponses);
    }
}
