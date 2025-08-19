
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ProductTableResponse;
import com.biobac.warehouse.response.WarehouseTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ProductService {

    Pair<List<ProductTableResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                Integer page,
                                                                Integer size,
                                                                String sortBy,
                                                                String sortDir);


    ProductDto getById(Long id);


    ProductDto create(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);


    void delete(Long id);
}
