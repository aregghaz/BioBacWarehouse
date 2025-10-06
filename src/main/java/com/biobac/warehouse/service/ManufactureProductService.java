package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ManufactureProductRequest;
import com.biobac.warehouse.response.ManufactureProductResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ManufactureProductService {

    @Transactional
    ManufactureProductResponse createForProduct(ManufactureProductRequest request);

    @Transactional(readOnly = true)
    Pair<List<ManufactureProductResponse>, PaginationMetadata> getByProductId(Long productId, Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir);
}
