package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.entity.ComponentType;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.TransferResponse;
import com.biobac.warehouse.service.TransferService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping("/product")
    public ApiResponse<String> transferProduct(@RequestBody List<TransferComponentDto> request) {
        transferService.transferProduct(request);
        return ResponseUtil.success("Product transfer successfully");
    }

    @PostMapping("/ingredient")
    public ApiResponse<String> transferIngredient(@RequestBody List<TransferComponentDto> request) {
        transferService.transferIngredient(request);
        return ResponseUtil.success("Ingredient transfer successfully");
    }

    @PostMapping("/ingredient/all")
    public ApiResponse<List<TransferResponse>> getIngredientAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<TransferResponse>, PaginationMetadata> result = transferService.getPagination(filters, page, size, sortBy, sortDir, ComponentType.INGREDIENT);
        return ResponseUtil.success("Transfers retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PostMapping("/product/all")
    public ApiResponse<List<TransferResponse>> getProductAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                             @RequestParam(required = false, defaultValue = "10") Integer size,
                                                             @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                             @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                             @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<TransferResponse>, PaginationMetadata> result = transferService.getPagination(filters, page, size, sortBy, sortDir, ComponentType.PRODUCT);
        return ResponseUtil.success("Transfers retrieved successfully", result.getFirst(), result.getSecond());
    }
}
