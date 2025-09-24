package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeComponent;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.exception.DeleteException;
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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeItemServiceImpl implements RecipeItemService {

    private final RecipeItemRepository recipeItemRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final RecipeComponentRepository recipeComponentRepository;
    private final RecipeItemMapper mapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size <= 0 ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        String sd = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir;
        Sort sort = sd.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        if (safeSize > 1000) {
            log.warn("Requested page size {} is too large, capping to 1000", safeSize);
            safeSize = 1000;
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<RecipeItemTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                 Integer page,
                                                                                 Integer size,
                                                                                 String sortBy,
                                                                                 String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

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
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "recipeItemTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemResponse> getAll() {
        return recipeItemRepository.findAll()
                .stream()
                .filter(r -> (r.getProducts() == null || r.getProducts().isEmpty()))
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

        if (recipeItem.getProducts() != null && !recipeItem.getProducts().isEmpty()) {
            throw new DeleteException("Recipe item used for product");
        }

        if (recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty()) {
            recipeComponentRepository.deleteAll(recipeItem.getComponents());
        }
        recipeItemRepository.delete(recipeItem);
    }
}