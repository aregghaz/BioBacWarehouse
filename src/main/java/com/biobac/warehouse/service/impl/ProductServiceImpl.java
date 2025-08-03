
package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.mapper.RecipeItemMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductMapper mapper;
    private final RecipeItemMapper recipeItemMapper;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    @Override
    public List<ProductDto> getAll() {
        return productRepo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    @Override
    public ProductDto getById(Long id) {
        return mapper.toDto(productRepo.findById(id).orElseThrow());
    }

    @Transactional
    @Override
    public ProductDto create(ProductDto dto) {
        Product product = mapper.toEntity(dto);
        if (dto.getIngredientIds() != null) {
            List<Ingredient> ingredients = ingredientRepo.findAllById(dto.getIngredientIds());
            product.setIngredients(ingredients);
        }
        
        
        // Save the product first to get its ID
        Product savedProduct = productRepo.save(product);
        
        // Process recipe items if provided
        if (dto.getRecipeItems() != null && !dto.getRecipeItems().isEmpty()) {
            List<RecipeItem> recipeItems = new ArrayList<>();
            for (RecipeItemDto recipeItemDto : dto.getRecipeItems()) {
                RecipeItem recipeItem = recipeItemMapper.toEntity(recipeItemDto);
                recipeItem.setProduct(savedProduct);
                
                // Set the ingredient
                Ingredient ingredient = ingredientRepo.findById(recipeItemDto.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + recipeItemDto.getIngredientId()));
                recipeItem.setIngredient(ingredient);
                
                recipeItems.add(recipeItem);
            }
            savedProduct.setRecipeItems(recipeItems);
            savedProduct = productRepo.save(savedProduct);
        }
        
        // Create inventory item if initialQuantity and warehouseId are provided
        if (dto.getInitialQuantity() != null && dto.getWarehouseId() != null) {
            InventoryItemDto inventoryItemDto = new InventoryItemDto();
            inventoryItemDto.setProductId(savedProduct.getId());
            inventoryItemDto.setWarehouseId(dto.getWarehouseId());
            inventoryItemDto.setQuantity(dto.getInitialQuantity());
            inventoryItemDto.setLastUpdated(LocalDate.now());
            inventoryService.create(inventoryItemDto);
        }
        
        return mapper.toDto(savedProduct);
    }

    @Transactional
    @Override
    public ProductDto update(Long id, ProductDto dto) {
        Product product = productRepo.findById(id).orElseThrow();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        if (dto.getIngredientIds() != null) {
            List<Ingredient> ingredients = ingredientRepo.findAllById(dto.getIngredientIds());
            product.setIngredients(ingredients);
        }
        
        
        // Update recipe items if provided
        if (dto.getRecipeItems() != null) {
            // Clear existing recipe items
            if (product.getRecipeItems() != null) {
                product.getRecipeItems().clear();
            } else {
                product.setRecipeItems(new ArrayList<>());
            }
            
            // Add new recipe items
            for (RecipeItemDto recipeItemDto : dto.getRecipeItems()) {
                RecipeItem recipeItem = recipeItemMapper.toEntity(recipeItemDto);
                recipeItem.setProduct(product);
                
                // Set the ingredient
                Ingredient ingredient = ingredientRepo.findById(recipeItemDto.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + recipeItemDto.getIngredientId()));
                recipeItem.setIngredient(ingredient);
                
                product.getRecipeItems().add(recipeItem);
            }
        }
        
        return mapper.toDto(productRepo.save(product));
    }
    @Transactional
    @Override
    public void delete(Long id) {
        productRepo.deleteById(id);
    }
}
