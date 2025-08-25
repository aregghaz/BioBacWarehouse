package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.RecipeComponent;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.mapper.RecipeItemMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.RecipeItemRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.RecipeComponentRequest;
import com.biobac.warehouse.request.RecipeItemCreateRequest;
import com.biobac.warehouse.response.RecipeItemResponse;
import com.biobac.warehouse.response.RecipeItemTableResponse;
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

import java.util.ArrayList;
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
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeItemDto getRecipeItemById(Long id) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDto> getRecipeItemsByProductId(Long productId) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDto> getRecipeItemsByIngredientId(Long ingredientId) {
        return null;
    }

    @Override
    public RecipeItemResponse createRecipeItem(RecipeItemCreateRequest recipeItemCreateRequest) {
        RecipeItem recipeItem = new RecipeItem();
        recipeItem.setName(recipeItemCreateRequest.getName());
        recipeItem.setNotes(recipeItemCreateRequest.getNotes());

        List<RecipeComponent> components = new ArrayList<>();
        for (RecipeComponentRequest compReq : recipeItemCreateRequest.getComponents()) {
            Ingredient ingredient = ingredientRepository.findById(compReq.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid ingredient ID: " + compReq.getIngredientId()));

            RecipeComponent component = new RecipeComponent();
            component.setIngredient(ingredient);
            component.setRecipeItem(recipeItem);
            component.setQuantity(compReq.getQuantity());

            components.add(component);
        }

        recipeItem.setComponents(components);

        RecipeItem saved = recipeItemRepository.save(recipeItem);

        return mapper.toDto(saved);
    }

    @Override
    public RecipeItemDto updateRecipeItem(Long id, RecipeItemDto recipeItemDto) {
        return null;
    }

    @Override
    public void deleteRecipeItem(Long id) {

    }


}