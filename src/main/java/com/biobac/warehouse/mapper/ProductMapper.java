package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.dto.RecipeItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RecipeItemMapper.class})
public interface ProductMapper {

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredientIds", expression = "java(mapIngredientListToIdList(product.getIngredients()))"),
            @Mapping(target = "recipeItems", source = "recipeItems"),
            @Mapping(target = "parentProductId", source = "parentProduct.id"),
            @Mapping(target = "childProductIds", expression = "java(mapChildProductIds(product))")
    })
    ProductDto toDto(Product product);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredients", ignore = true), // manually set later in service
            @Mapping(target = "recipeItems", ignore = true),  // manually set later in service
            @Mapping(target = "parentProduct", ignore = true),
            @Mapping(target = "childProducts", ignore = true)
    })
    Product toEntity(ProductDto dto);

    default List<Long> mapIngredientListToIdList(List<Ingredient> ingredients) {
        if (ingredients == null) return null;
        return ingredients.stream().map(Ingredient::getId).collect(Collectors.toList());
    }
    
    default List<Long> mapChildProductIds(Product product) {
        if (product.getChildProducts() == null) {
            return new ArrayList<>();
        }
        return product.getChildProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList());
    }
    
    @AfterMapping
    default void setParentProduct(@MappingTarget Product entity, ProductDto dto) {
        if (dto.getParentProductId() != null) {
            Product parentProduct = new Product();
            parentProduct.setId(dto.getParentProductId());
            entity.setParentProduct(parentProduct);
        }
    }
}
