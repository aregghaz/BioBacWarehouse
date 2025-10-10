package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.WarehouseTypeRequest;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface WarehouseTypeService {
    Pair<List<WarehouseTypeResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                        Integer page,
                                                                        Integer size,
                                                                        String sortBy,
                                                                        String sortDir);

    WarehouseTypeResponse getById(Long id);

    List<WarehouseTypeResponse> getAll();

    WarehouseTypeResponse create(WarehouseTypeRequest request);

    WarehouseTypeResponse update(Long id, WarehouseTypeRequest request);

    void delete(Long id);
}
