
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.response.ProductTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ProductService {

    @Transactional(readOnly = true)
    List<ProductDto> getAll();

    @Transactional(readOnly = true)
    Pair<List<ProductTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                       Integer page,
                                                                       Integer size,
                                                                       String sortBy,
                                                                       String sortDir);


    @Transactional(readOnly = true)
    ProductDto getById(Long id);


    @Transactional
    ProductDto create(ProductCreateRequest dto);

    @Transactional
    ProductDto update(Long id, ProductDto dto);

    @Transactional
    ProductDto update(Long id, ProductUpdateRequest dto);

    @Transactional
    void delete(Long id);
}
