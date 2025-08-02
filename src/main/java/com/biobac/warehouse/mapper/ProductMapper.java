package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(source = "ingredients", target = "ingredientIds", qualifiedByName = "mapIngredientListToIdList")
    })
    ProductDto toDto(Product product);

    @Mappings({
            @Mapping(target = "ingredients", ignore = true) // manually set later in service
    })
    Product toEntity(ProductDto dto);

    @Named("mapIngredientListToIdList")
    static List<Long> mapIngredientListToIdList(List<Ingredient> ingredients) {
        if (ingredients == null) return null;
        return ingredients.stream().map(Ingredient::getId).collect(Collectors.toList());
    }
}
