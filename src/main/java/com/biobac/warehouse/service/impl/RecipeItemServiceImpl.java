package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.RecipeItemMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.RecipeItemRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.RecipeItemTableResponse;
import com.biobac.warehouse.response.WarehouseTableResponse;
import com.biobac.warehouse.service.RecipeItemService;
import com.biobac.warehouse.utils.specifications.RecipeItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeItemServiceImpl implements RecipeItemService {

    private final RecipeItemRepository recipeItemRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeItemMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pair<List<RecipeItemTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                 Integer page,
                                                                                 Integer size,
                                                                                 String sortBy,
                                                                                 String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<RecipeItem> spec = RecipeItemSpecification.buildSpecification(filters);

        Page<RecipeItem> recipeItemPage = recipeItemRepository.findAll(spec, pageable);

        List<RecipeItemTableResponse> content = recipeItemPage.getContent()
                .stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                recipeItemPage.getNumber(),
                recipeItemPage.getSize(),
                recipeItemPage.getTotalElements(),
                recipeItemPage.getTotalPages(),
                recipeItemPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "recipeItemTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDto> getAll() {
        return recipeItemRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeItemDto getRecipeItemById(Long id) {
        RecipeItem recipeItem = recipeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RecipeItem not found with id: " + id));
        return mapper.toDto(recipeItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDto> getRecipeItemsByProductId(Long productId) {
        return recipeItemRepository.findByProductId(productId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDto> getRecipeItemsByIngredientId(Long ingredientId) {
        return recipeItemRepository.findByIngredientId(ingredientId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecipeItemDto createRecipeItem(RecipeItemDto recipeItemDto, Long productId) {
        RecipeItem recipeItem = mapper.toEntity(recipeItemDto);

        // Set the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        recipeItem.setProduct(product);

        // Set the ingredient
        Ingredient ingredient = ingredientRepository.findById(recipeItemDto.getIngredientId())
                .orElseThrow(() -> new NotFoundException("Ingredient not found with id: " + recipeItemDto.getIngredientId()));
        recipeItem.setIngredient(ingredient);

        RecipeItem savedRecipeItem = recipeItemRepository.save(recipeItem);
        return mapper.toDto(savedRecipeItem);
    }

    @Override
    @Transactional
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
        return mapper.toDto(updatedRecipeItem);
    }

    @Override
    @Transactional
    public void deleteRecipeItem(Long id) {
        if (!recipeItemRepository.existsById(id)) {
            throw new NotFoundException("RecipeItem not found with id: " + id);
        }
        recipeItemRepository.deleteById(id);
    }
}