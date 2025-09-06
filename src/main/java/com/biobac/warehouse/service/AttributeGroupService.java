package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AttributeGroupResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface AttributeGroupService {

    @Transactional(readOnly = true)
    List<AttributeGroupResponse> getAll();

    @Transactional(readOnly = true)
    Pair<List<AttributeGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir);

    @Transactional(readOnly = true)
    AttributeGroupResponse getById(Long id);

    @Transactional
    AttributeGroupResponse create(AttributeGroupCreateRequest group);

    @Transactional
    AttributeGroupResponse update(Long id, AttributeGroupCreateRequest group);

    @Transactional
    void delete(Long id);
}
