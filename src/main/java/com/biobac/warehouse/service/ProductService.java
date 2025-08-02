
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductMapper mapper;

    public List<ProductDto> getAll() {
        return productRepo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto getById(Long id) {
        return mapper.toDto(productRepo.findById(id).orElseThrow());
    }

    public ProductDto create(ProductDto dto) {
        Product product = mapper.toEntity(dto);
        if (dto.getIngredientIds() != null) {
            List<Ingredient> ingredients = ingredientRepo.findAllById(dto.getIngredientIds());
            product.setIngredients(ingredients);
        }
        return mapper.toDto(productRepo.save(product));
    }

    public ProductDto update(Long id, ProductDto dto) {
        Product product = productRepo.findById(id).orElseThrow();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        if (dto.getIngredientIds() != null) {
            List<Ingredient> ingredients = ingredientRepo.findAllById(dto.getIngredientIds());
            product.setIngredients(ingredients);
        }
        return mapper.toDto(productRepo.save(product));
    }

    public void delete(Long id) {
        productRepo.deleteById(id);
    }
}
