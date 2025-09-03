package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductGroupDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ProductGroupResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ProductGroupService {

    @Transactional(readOnly = true)
    List<ProductGroupResponse> getPagination();

    @Transactional(readOnly = true)
    Pair<List<ProductGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                       Integer page,
                                                                       Integer size,
                                                                       String sortBy,
                                                                       String sortDir);

    @Transactional(readOnly = true)
    ProductGroupResponse getById(Long id);

    @Transactional
    ProductGroupResponse create(ProductGroupDto dto);

    @Transactional
    ProductGroupResponse update(Long id, ProductGroupDto dto);

    @Transactional
    void delete(Long id);
}
