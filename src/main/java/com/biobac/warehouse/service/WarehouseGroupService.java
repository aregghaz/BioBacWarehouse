package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseGroupDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.WarehouseGroupResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface WarehouseGroupService {
    List<WarehouseGroupResponse> getAll();

    Pair<List<WarehouseGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir);

    WarehouseGroupResponse getById(Long id);

    WarehouseGroupResponse create(WarehouseGroupDto dto);

    WarehouseGroupResponse update(Long id, WarehouseGroupDto dto);

    void delete(Long id);
}
