package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.RecipeItemMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.RecipeItemRepository;
import com.biobac.warehouse.service.RecipeItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeItemServiceImpl implements RecipeItemService {

    private final RecipeItemRepository recipeItemRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeItemMapper recipeItemMapper;

    @Override
    public List<RecipeItemDto> getAllRecipeItems() {
        return recipeItemRepository.findAll().stream()
                .map(recipeItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeItemDto getRecipeItemById(Long id) {
        RecipeItem recipeItem = recipeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RecipeItem not found with id: " + id));
        return recipeItemMapper.toDto(recipeItem);
    }

    @Override
    public List<RecipeItemDto> getRecipeItemsByProductId(Long productId) {
        return recipeItemRepository.findByProductId(productId).stream()
                .map(recipeItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeItemDto> getRecipeItemsByIngredientId(Long ingredientId) {
        return recipeItemRepository.findByIngredientId(ingredientId).stream()
                .map(recipeItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeItemDto createRecipeItem(RecipeItemDto recipeItemDto, Long productId) {
        RecipeItem recipeItem = recipeItemMapper.toEntity(recipeItemDto);
        
        // Set the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        recipeItem.setProduct(product);
        
        // Set the ingredient
        Ingredient ingredient = ingredientRepository.findById(recipeItemDto.getIngredientId())
                .orElseThrow(() -> new NotFoundException("Ingredient not found with id: " + recipeItemDto.getIngredientId()));
        recipeItem.setIngredient(ingredient);
        
        RecipeItem savedRecipeItem = recipeItemRepository.save(recipeItem);
        return recipeItemMapper.toDto(savedRecipeItem);
    }

    @Override
    public RecipeItemDto updateRecipeItem(Long id, RecipeItemDto recipeItemDto) {
        RecipeItem existingRecipeItem = recipeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RecipeItem not found with id: " + id));
        
        // Update fields
        existingRecipeItem.setQuantity(recipeItemDto.getQuantity());
        existingRecipeItem.setNotes(recipeItemDto.getNotes());
        
        // Update ingredient if changed
        if (!existingRecipeItem.getIngredient().getId().equals(recipeItemDto.getIngredientId())) {
            Ingredient ingredient = ingredientRepository.findById(recipeItemDto.getIngredientId())
                    .orElseThrow(() -> new NotFoundException("Ingredient not found with id: " + recipeItemDto.getIngredientId()));
            existingRecipeItem.setIngredient(ingredient);
        }
        
        RecipeItem updatedRecipeItem = recipeItemRepository.save(existingRecipeItem);
        return recipeItemMapper.toDto(updatedRecipeItem);
    }

    @Override
    public void deleteRecipeItem(Long id) {
        if (!recipeItemRepository.existsById(id)) {
            throw new NotFoundException("RecipeItem not found with id: " + id);
        }
        recipeItemRepository.deleteById(id);
    }
}