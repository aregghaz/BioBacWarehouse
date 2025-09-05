package com.biobac.warehouse.service;

import com.biobac.warehouse.entity.AttributeGroup;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.response.AttributeGroupResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AttributeGroupService {

    @Transactional(readOnly = true)
    List<AttributeGroupResponse> getAll();

    @Transactional(readOnly = true)
    AttributeGroupResponse getById(Long id);

    @Transactional
    AttributeGroupResponse create(AttributeGroupCreateRequest group);

    @Transactional
    AttributeGroupResponse update(Long id, AttributeGroupCreateRequest group);

    @Transactional
    void delete(Long id);
}
