package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.mapper.RecipeItemMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.InventoryItemRepository;
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
    private final InventoryItemRepository inventoryItemRepo;

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
        // Ensure quantity is not null for calculations
        if (dto.getQuantity() == null) {
            dto.setQuantity(1.0); // Default to 1 if not specified
        }
        System.out.println("[DEBUG_LOG] Creating product with name: " + dto.getName() + 
            ", quantity: " + dto.getQuantity() + 
            ", warehouseId: " + dto.getWarehouseId());
            
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
        
        // Create inventory item if quantity and warehouseId are provided
        if (dto.getQuantity() != null && dto.getWarehouseId() != null) {
            InventoryItemDto inventoryItemDto = new InventoryItemDto();
            inventoryItemDto.setProductId(savedProduct.getId());
            inventoryItemDto.setWarehouseId(dto.getWarehouseId());
            inventoryItemDto.setQuantity(dto.getQuantity() != null ? dto.getQuantity().intValue() : 0);
            inventoryItemDto.setLastUpdated(LocalDate.now());
            
            // Set the ingredient count based on the number of recipe items
            int ingredientCount = 0;
            if (savedProduct.getRecipeItems() != null && !savedProduct.getRecipeItems().isEmpty()) {
                ingredientCount = savedProduct.getRecipeItems().size();
            }
            inventoryItemDto.setIngredientCount(ingredientCount);
            
            inventoryService.create(inventoryItemDto);
            
            // Update ingredient counts in inventory for all recipe items
            System.out.println("[DEBUG_LOG] Checking recipe items for product: " + savedProduct.getId() + 
                ", warehouseId: " + dto.getWarehouseId());
                
            if (savedProduct.getRecipeItems() != null && !savedProduct.getRecipeItems().isEmpty()) {
                System.out.println("[DEBUG_LOG] Product has " + savedProduct.getRecipeItems().size() + " recipe items");
                
                // Use the new method to update ingredient counts
                updateIngredientCounts(savedProduct.getRecipeItems(), dto.getWarehouseId());
                
                for (RecipeItem recipeItem : savedProduct.getRecipeItems()) {
                    Long ingredientId = recipeItem.getIngredient().getId();
                    System.out.println("[DEBUG_LOG] Processing recipe item with ingredient ID: " + ingredientId);
                    
                    if (dto.getWarehouseId() != null) {
                        // Reduce the ingredient quantity in inventory
                        List<InventoryItem> inventoryItems = inventoryItemRepo.findByIngredientIdAndWarehouseId(
                            ingredientId, dto.getWarehouseId());
                            
                        if (!inventoryItems.isEmpty()) {
                            for (InventoryItem item : inventoryItems) {
                                // Calculate the amount to decrease based on recipe item quantity AND product quantity
                                // This ensures quantity changes every time a product is created
                                int amountToDecrease = (int) Math.ceil(recipeItem.getQuantity() * dto.getQuantity());
                                
                                System.out.println("[DEBUG_LOG] Reducing inventory for ingredient ID: " + ingredientId + 
                                    " by " + amountToDecrease + " units");
                                
                                // Use the repository method to decrement the quantity
                                int updatedRows = inventoryItemRepo.decrementIngredientQuantity(ingredientId, dto.getWarehouseId(), amountToDecrease);
                                
                                if (updatedRows > 0) {
                                    System.out.println("[DEBUG_LOG] Successfully updated " + updatedRows + " inventory items");
                                    
                                    // Update the ingredient's quantity field as well
                                    Ingredient ingredient = recipeItem.getIngredient();
                                    if (ingredient != null && ingredient.getQuantity() != null) {
                                        double newQuantity = ingredient.getQuantity() - amountToDecrease;
                                        ingredient.setQuantity(newQuantity >= 0 ? newQuantity : 0);
                                        ingredientRepo.save(ingredient);
                                        System.out.println("[DEBUG_LOG] Updated ingredient quantity to: " + ingredient.getQuantity());
                                    }
                                    
                                    // Refresh the item to get the updated quantity
                                    item = inventoryItemRepo.findById(item.getId()).orElse(item);
                                    System.out.println("[DEBUG_LOG] Updated inventory quantity to: " + item.getQuantity());
                                } else {
                                    System.out.println("[DEBUG_LOG] Warning: Not enough inventory for ingredient ID: " + 
                                        ingredientId + ". Required: " + amountToDecrease + ", Available: " + item.getQuantity());
                                }
                            }
                        }
                    } else {
                        System.out.println("[DEBUG_LOG] Warehouse ID is null, cannot update ingredient count");
                    }
                    
                    // Check if there are any inventory items for this ingredient in this warehouse
                    List<InventoryItem> checkItems = inventoryItemRepo.findByIngredientIdAndWarehouseId(
                        ingredientId, dto.getWarehouseId());
                    if (checkItems.isEmpty()) {
                        System.out.println("[DEBUG_LOG] No inventory items found for ingredient in this warehouse");
                    }
                }
            } else {
                System.out.println("[DEBUG_LOG] Product has no recipe items");
            }
        }
        
        // Create the response DTO and set the quantity and warehouseId
        ProductDto responseDto = mapper.toDto(savedProduct);
        System.out.println("[DEBUG_LOG] Setting quantity in response: " + dto.getQuantity());
        System.out.println("[DEBUG_LOG] Setting warehouseId in response: " + dto.getWarehouseId());
        responseDto.setQuantity(dto.getQuantity());
        responseDto.setWarehouseId(dto.getWarehouseId());
        System.out.println("[DEBUG_LOG] Response after setting values - quantity: " + responseDto.getQuantity() + ", warehouseId: " + responseDto.getWarehouseId());
        
        return responseDto;
    }

    @Transactional
    @Override
    public ProductDto update(Long id, ProductDto dto) {
        // Ensure quantity is not null for calculations
        if (dto.getQuantity() == null) {
            dto.setQuantity(1.0); // Default to 1 if not specified
        }
        
        System.out.println("[DEBUG_LOG] Updating product with ID: " + id + 
            ", name: " + dto.getName() + 
            ", quantity: " + dto.getQuantity() + 
            ", warehouseId: " + dto.getWarehouseId());
            
        Product product = productRepo.findById(id).orElseThrow();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        // Store the quantity and warehouseId for later use in the response
        Double quantity = dto.getQuantity();
        Long warehouseId = dto.getWarehouseId();
        
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
        
        Product savedProduct = productRepo.save(product);
        
        // Update inventory item if quantity and warehouseId are provided
        if (dto.getQuantity() != null && dto.getWarehouseId() != null) {
            System.out.println("[DEBUG_LOG] Updating inventory for product ID: " + id + 
                ", quantity: " + dto.getQuantity() + 
                ", warehouseId: " + dto.getWarehouseId());
                
            // Check if inventory item exists for this product and warehouse
            List<InventoryItem> existingItems = inventoryItemRepo.findByProductId(savedProduct.getId());
            InventoryItem inventoryItem = null;
            
            for (InventoryItem item : existingItems) {
                if (item.getWarehouseId() != null && item.getWarehouseId().equals(dto.getWarehouseId())) {
                    inventoryItem = item;
                    break;
                }
            }
            
            if (inventoryItem != null) {
                // Update existing inventory item
                System.out.println("[DEBUG_LOG] Updating existing inventory item ID: " + inventoryItem.getId());
                InventoryItemDto inventoryItemDto = new InventoryItemDto();
                inventoryItemDto.setId(inventoryItem.getId());
                inventoryItemDto.setProductId(savedProduct.getId());
                inventoryItemDto.setWarehouseId(dto.getWarehouseId());
                inventoryItemDto.setQuantity(dto.getQuantity() != null ? dto.getQuantity().intValue() : 0);
                inventoryItemDto.setLastUpdated(LocalDate.now());
                
                // Set the ingredient count based on the number of recipe items
                int ingredientCount = 0;
                if (savedProduct.getRecipeItems() != null && !savedProduct.getRecipeItems().isEmpty()) {
                    ingredientCount = savedProduct.getRecipeItems().size();
                }
                inventoryItemDto.setIngredientCount(ingredientCount);
                
                inventoryService.update(inventoryItem.getId(), inventoryItemDto);
            } else {
                // Create new inventory item
                System.out.println("[DEBUG_LOG] Creating new inventory item for product ID: " + savedProduct.getId());
                InventoryItemDto inventoryItemDto = new InventoryItemDto();
                inventoryItemDto.setProductId(savedProduct.getId());
                inventoryItemDto.setWarehouseId(dto.getWarehouseId());
                inventoryItemDto.setQuantity(dto.getQuantity() != null ? dto.getQuantity().intValue() : 0);
                inventoryItemDto.setLastUpdated(LocalDate.now());
                
                // Set the ingredient count based on the number of recipe items
                int ingredientCount = 0;
                if (savedProduct.getRecipeItems() != null && !savedProduct.getRecipeItems().isEmpty()) {
                    ingredientCount = savedProduct.getRecipeItems().size();
                }
                inventoryItemDto.setIngredientCount(ingredientCount);
                
                inventoryService.create(inventoryItemDto);
            }
            
            // Update ingredient counts in inventory for all recipe items
            System.out.println("[DEBUG_LOG] Checking recipe items for product: " + savedProduct.getId() + 
                ", warehouseId: " + dto.getWarehouseId());
                
            if (savedProduct.getRecipeItems() != null && !savedProduct.getRecipeItems().isEmpty()) {
                System.out.println("[DEBUG_LOG] Product has " + savedProduct.getRecipeItems().size() + " recipe items");
                
                // Use the new method to update ingredient counts
                updateIngredientCounts(savedProduct.getRecipeItems(), dto.getWarehouseId());
                
                for (RecipeItem recipeItem : savedProduct.getRecipeItems()) {
                    Long ingredientId = recipeItem.getIngredient().getId();
                    System.out.println("[DEBUG_LOG] Processing recipe item with ingredient ID: " + ingredientId);
                    
                    if (dto.getWarehouseId() != null) {
                        // Reduce the ingredient quantity in inventory
                        List<InventoryItem> inventoryItems = inventoryItemRepo.findByIngredientIdAndWarehouseId(
                            ingredientId, dto.getWarehouseId());
                            
                        if (!inventoryItems.isEmpty()) {
                            for (InventoryItem item : inventoryItems) {
                                // Calculate the amount to decrease based on recipe item quantity AND product quantity
                                // This ensures quantity changes every time a product is updated
                                int amountToDecrease = (int) Math.ceil(recipeItem.getQuantity() * dto.getQuantity());
                                
                                System.out.println("[DEBUG_LOG] Reducing inventory for ingredient ID: " + ingredientId + 
                                    " by " + amountToDecrease + " units");
                                
                                // Use the repository method to decrement the quantity
                                int updatedRows = inventoryItemRepo.decrementIngredientQuantity(ingredientId, dto.getWarehouseId(), amountToDecrease);
                                
                                if (updatedRows > 0) {
                                    System.out.println("[DEBUG_LOG] Successfully updated " + updatedRows + " inventory items");
                                    
                                    // Update the ingredient's quantity field as well
                                    Ingredient ingredient = recipeItem.getIngredient();
                                    if (ingredient != null && ingredient.getQuantity() != null) {
                                        double newQuantity = ingredient.getQuantity() - amountToDecrease;
                                        ingredient.setQuantity(newQuantity >= 0 ? newQuantity : 0);
                                        ingredientRepo.save(ingredient);
                                        System.out.println("[DEBUG_LOG] Updated ingredient quantity to: " + ingredient.getQuantity());
                                    }
                                    
                                    // Refresh the item to get the updated quantity
                                    item = inventoryItemRepo.findById(item.getId()).orElse(item);
                                    System.out.println("[DEBUG_LOG] Updated inventory quantity to: " + item.getQuantity());
                                } else {
                                    System.out.println("[DEBUG_LOG] Warning: Not enough inventory for ingredient ID: " + 
                                        ingredientId + ". Required: " + amountToDecrease + ", Available: " + item.getQuantity());
                                }
                            }
                        }
                    } else {
                        System.out.println("[DEBUG_LOG] Warehouse ID is null, cannot update ingredient count");
                    }
                    
                    // Check if there are any inventory items for this ingredient in this warehouse
                    List<InventoryItem> checkItems = inventoryItemRepo.findByIngredientIdAndWarehouseId(
                        ingredientId, dto.getWarehouseId());
                    if (checkItems.isEmpty()) {
                        System.out.println("[DEBUG_LOG] No inventory items found for ingredient in this warehouse");
                    }
                }
            } else {
                System.out.println("[DEBUG_LOG] Product has no recipe items");
            }
        }
        
        // Create the response DTO and set the quantity and warehouseId
        ProductDto responseDto = mapper.toDto(savedProduct);
        responseDto.setQuantity(quantity);
        responseDto.setWarehouseId(warehouseId);
        
        return responseDto;
    }

    @Transactional
    @Override
    public void delete(Long id) {
        productRepo.deleteById(id);
    }
    
    /**
     * Updates ingredient counts for all ingredients in a recipe.
     * This ensures that the ingredient count is set to the total number of recipe items,
     * rather than incrementing for each recipe item.
     * 
     * @param recipeItems the list of recipe items
     * @param warehouseId the warehouse ID
     */
    private void updateIngredientCounts(List<RecipeItem> recipeItems, Long warehouseId) {
        if (recipeItems == null || recipeItems.isEmpty() || warehouseId == null) {
            return;
        }
        
        // Get the total number of recipe items
        int recipeItemCount = recipeItems.size();
        System.out.println("[DEBUG_LOG] Setting ingredient count to " + recipeItemCount + " for all ingredients in recipe");
        
        // Process each recipe item
        for (RecipeItem recipeItem : recipeItems) {
            Long ingredientId = recipeItem.getIngredient().getId();
            
            // Check if the ingredient already has inventory items with a product
            List<InventoryItem> existingItems = inventoryItemRepo.findByIngredientIdAndWarehouseId(
                ingredientId, warehouseId);
            
            boolean hasProductAlready = false;
            for (InventoryItem item : existingItems) {
                if (item.getProduct() != null) {
                    hasProductAlready = true;
                    System.out.println("[DEBUG_LOG] Ingredient ID: " + ingredientId + " already has a product associated with it");
                    break;
                }
            }
            
            // Only update the ingredient count if it doesn't already have a product
            if (!hasProductAlready) {
                // Use the direct update method to set the ingredient count
                int updatedCount = inventoryItemRepo.setIngredientCount(ingredientId, warehouseId, recipeItemCount);
                System.out.println("[DEBUG_LOG] Updated " + updatedCount + " inventory items for ingredient ID: " + ingredientId);
                
                // If no rows were updated, it might be because the inventory item doesn't exist
                if (updatedCount == 0) {
                    for (InventoryItem item : existingItems) {
                        System.out.println("[DEBUG_LOG] Setting ingredient count to " + recipeItemCount + " for item ID: " + item.getId());
                        item.setIngredientCount(recipeItemCount);
                        inventoryItemRepo.save(item);
                    }
                }
            } else {
                System.out.println("[DEBUG_LOG] Skipping ingredient count update for ingredient ID: " + ingredientId + " as it already has a product");
            }
        }
    }
}