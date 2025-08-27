package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.InventoryItemRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.RecipeItemRepository;
import com.biobac.warehouse.repository.WarehouseRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.specifications.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setCompanyId(request.getCompanyId());

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
            product.setRecipeItem(recipeItem);
            for (RecipeComponent component : recipeItem.getComponents()) {
                Ingredient ingredient = component.getIngredient();
                double requiredQty = component.getQuantity();

                List<InventoryItem> inventories = ingredient.getInventoryItems();

                double totalAvailable = inventories.stream()
                        .mapToDouble(InventoryItem::getQuantity)
                        .sum();

                if (totalAvailable < requiredQty) {
                    throw new RuntimeException(
                            "Not enough stock for ingredient " + ingredient.getName()
                    );
                }

                double remainingToDeduct = requiredQty;
                for (InventoryItem inv : inventories) {
                    double deduct = Math.min(inv.getQuantity(), remainingToDeduct);
                    inv.setQuantity(inv.getQuantity() - deduct);
                    inventoryItemRepository.save(inv);

                    remainingToDeduct -= deduct;
                    if (remainingToDeduct <= 0) break;
                }
            }

        }

        List<InventoryItem> inventoryItems = new ArrayList<>();
        if (request.getWarehouseId() != null) {

            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));

            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.setProduct(product);
            inventoryItem.setWarehouse(warehouse);
            inventoryItem.setQuantity(request.getQuantity() != null ? request.getQuantity() : 0.0);
            inventoryItem.setLastUpdated(LocalDateTime.now());

            inventoryItems.add(inventoryItem);

        }
        product.setInventoryItems(inventoryItems);

        Product saved = productRepository.save(product);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductCreateRequest request) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Product> spec = ProductSpecification.buildSpecification(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "productTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());

        List<Long> warehouseIds = product.getInventoryItems().stream()
                .map(inv -> inv.getWarehouse() != null ? inv.getWarehouse().getId() : null)
                .toList();
        response.setWarehouseId(warehouseIds);

        double totalQuantity = product.getInventoryItems().stream()
                .mapToDouble(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                .sum();
        response.setQuantity(totalQuantity);

        return response;
    }

}