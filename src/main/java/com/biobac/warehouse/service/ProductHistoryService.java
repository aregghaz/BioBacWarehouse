package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.request.FilterCriteria;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface ProductHistoryService {

    ProductHistoryDto recordQuantityChange(Product product, Double quantityBefore,
                                           Double quantityAfter, String action, String notes);

    Pair<List<ProductHistoryDto>, PaginationMetadata> getHistoryForProduct(Long productId, Map<String, FilterCriteria> filters,
                                                                           Integer page, Integer size, String sortBy, String sortDir);

    Pair<List<ProductHistoryDto>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters,
                                                                 Integer page, Integer size, String sortBy, String sortDir);
}