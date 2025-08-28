package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.entity.Unit;
import com.biobac.warehouse.request.FilterCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.biobac.warehouse.utils.SpecificationUtil.*;

public class ProductSpecification {
    private static String isIngredientField(String field) {
        Map<String, String> ingredientField = Map.of(
                "ingredientId", "id",
                "ingredientName", "name"
        );
        return ingredientField.getOrDefault(field, null);
    }

    private static String isRecipeItemField(String field) {
        Map<String, String> recipeItemField = Map.of(
                "recipeItemId", "id",
                "recipeItemName", "name"
        );
        return recipeItemField.getOrDefault(field, null);
    }

    private static String isUnitField(String field) {
        Map<String, String> unitField = Map.of(
                "unitId", "id",
                "unitName", "name"
        );
        return unitField.getOrDefault(field, null);
    }

    public static Specification<Product> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            Join<Product, RecipeItem> recipeItemJoin = null;
            Join<Product, Ingredient> ingredientJoin = null;
            Join<Product, Unit> unitJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Path<?> path;
                    Predicate predicate = null;

                    if (isIngredientField(field) != null) {
                        if (ingredientJoin == null) {
                            ingredientJoin = root.join("ingredients", JoinType.LEFT);
                        }
                        path = ingredientJoin.get(isIngredientField(field));
                    } else if (isRecipeItemField(field) != null) {
                        if (recipeItemJoin == null) {
                            recipeItemJoin = root.join("recipeItems", JoinType.LEFT);
                        }
                        path = recipeItemJoin.get(isRecipeItemField(field));
                    } else if (isUnitField(field) != null) {
                        if (unitJoin == null) {
                            unitJoin = root.join("unit", JoinType.LEFT);
                        }
                        path = unitJoin.get(isUnitField(field));
                    } else {
                        path = root.get(field);
                    }

                    switch (criteria.getOperator()) {
                        case "equals" -> predicate = buildEquals(cb, path, criteria.getValue());
                        case "notEquals" -> predicate = buildNotEquals(cb, path, criteria.getValue());
                        case "contains" -> predicate = buildContains(cb, path, criteria.getValue());
                        case "greaterThanOrEqualTo" ->
                                predicate = buildGreaterThanOrEqualTo(cb, path, criteria.getValue());
                        case "lessThanOrEqualTo" -> predicate = buildLessThanOrEqualTo(cb, path, criteria.getValue());
                        case "between" -> predicate = buildBetween(cb, path, criteria.getValue());
                    }

                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
