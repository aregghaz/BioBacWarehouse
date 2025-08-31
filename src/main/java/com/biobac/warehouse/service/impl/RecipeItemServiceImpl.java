package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeComponent;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.RecipeItemMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.RecipeComponentRepository;
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
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final RecipeComponentRepository recipeComponentRepository;
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
    public List<RecipeItemResponse> getAll() {
        return recipeItemRepository.findAll()
                .stream()
                .filter(r -> r.getIngredient() == null && r.getProduct() == null)
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeItemResponse getRecipeItemById(Long id) {
        RecipeItem recipeItem = recipeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe item not found"));
        return mapper.toDto(recipeItem);
    }


    @Override
    @Transactional
    public RecipeItemResponse createRecipeItem(RecipeItemCreateRequest recipeItemCreateRequest) {
        RecipeItem recipeItem = new RecipeItem();
        recipeItem.setName(recipeItemCreateRequest.getName());
        recipeItem.setNotes(recipeItemCreateRequest.getNotes());

        RecipeItem savedParent = recipeItemRepository.save(recipeItem);

        List<RecipeComponent> components = new ArrayList<>();
        for (RecipeComponentRequest compReq : recipeItemCreateRequest.getComponents()) {
            Long ingId = compReq.getIngredientId();
            Long prodId = compReq.getProductId();

            if ((ingId == null && prodId == null) || (ingId != null && prodId != null)) {
                throw new InvalidDataException("Exactly one of ingredientId or productId must be provided for each recipe component");
            }

            RecipeComponent component = new RecipeComponent();
            component.setRecipeItem(savedParent);
            component.setQuantity(compReq.getQuantity());

            if (ingId != null) {
                Ingredient ingredient = ingredientRepository.findById(ingId)
                        .orElseThrow(() -> new NotFoundException("Invalid ingredient ID: " + ingId));
                component.setIngredient(ingredient);
            } else {
                Product productComp = productRepository.findById(prodId)
                        .orElseThrow(() -> new NotFoundException("Invalid product ID: " + prodId));
                component.setProduct(productComp);
            }

            recipeComponentRepository.save(component);
            components.add(component);
        }

        savedParent.setComponents(components);

        return mapper.toDto(savedParent);
    }

    @Override
    @Transactional
    public RecipeItemResponse updateRecipeItem(Long id, RecipeItemCreateRequest recipeItemCreateRequest) {
        RecipeItem recipeItem = recipeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe item not found"));

        recipeItem.setName(recipeItemCreateRequest.getName());
        recipeItem.setNotes(recipeItemCreateRequest.getNotes());

        if (recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty()) {
            recipeComponentRepository.deleteAll(recipeItem.getComponents());
            recipeItem.getComponents().clear();
        }

        List<RecipeComponent> components = new ArrayList<>();
        for (RecipeComponentRequest compReq : recipeItemCreateRequest.getComponents()) {
            Long ingId = compReq.getIngredientId();
            Long prodId = compReq.getProductId();

            if ((ingId == null && prodId == null) || (ingId != null && prodId != null)) {
                throw new InvalidDataException("Exactly one of ingredientId or productId must be provided for each recipe component");
            }

            RecipeComponent component = new RecipeComponent();
            component.setRecipeItem(recipeItem);
            component.setQuantity(compReq.getQuantity());

            if (ingId != null) {
                Ingredient ingredient = ingredientRepository.findById(ingId)
                        .orElseThrow(() -> new NotFoundException("Invalid ingredient ID: " + ingId));
                component.setIngredient(ingredient);
            } else {
                Product productComp = productRepository.findById(prodId)
                        .orElseThrow(() -> new NotFoundException("Invalid product ID: " + prodId));
                component.setProduct(productComp);
            }

            recipeComponentRepository.save(component);
            components.add(component);
        }
        recipeItem.setComponents(components);

        RecipeItem saved = recipeItemRepository.save(recipeItem);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteRecipeItem(Long id) {
        RecipeItem recipeItem = recipeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe item not found"));

        if (recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty()) {
            recipeComponentRepository.deleteAll(recipeItem.getComponents());
        }
        recipeItemRepository.delete(recipeItem);
    }
}