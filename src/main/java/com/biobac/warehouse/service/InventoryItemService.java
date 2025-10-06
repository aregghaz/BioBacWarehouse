package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.InventoryItemResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface InventoryItemService {

    @Transactional(readOnly = true)
    Pair<List<InventoryItemResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                 Integer page,
                                                                 Integer size,
                                                                 String sortBy,
                                                                 String sortDir);

    @Transactional(readOnly = true)
    Pair<List<InventoryItemResponse>, PaginationMetadata> getByWarehouseId(Long warehouseId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);
}
