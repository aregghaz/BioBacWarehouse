
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public interface ProductService {

    List<ProductDto> getAll();


    ProductDto getById(Long id);


    ProductDto create(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);


    void delete(Long id);
}
