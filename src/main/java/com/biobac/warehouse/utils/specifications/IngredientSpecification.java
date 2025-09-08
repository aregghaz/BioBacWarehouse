package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
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

public class IngredientSpecification {

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

    private static String isIngredientGroupField(String field) {
        Map<String, String> ingredientGroupField = Map.of(
                "ingredientGroupId", "id",
                "ingredientGroupName", "name"
        );
        return ingredientGroupField.getOrDefault(field, null);
    }

    public static Specification<Ingredient> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Ingredient, Unit> unitJoin = null;
            Join<Ingredient, RecipeItem> recipeItemJoin = null;
            Join<Ingredient, IngredientGroup> ingredientGroupJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Path<?> path;

                    if (isRecipeItemField(field) != null) {
                        if (recipeItemJoin == null) {
                            recipeItemJoin = root.join("recipeItem", JoinType.LEFT);
                        }
                        path = recipeItemJoin.get(isRecipeItemField(field));
                    } else if (isUnitField(field) != null) {
                        if (unitJoin == null) {
                            unitJoin = root.join("unit", JoinType.LEFT);
                        }
                        path = unitJoin.get(isUnitField(field));
                    } else if (isIngredientGroupField(field) != null) {
                        if (ingredientGroupJoin == null) {
                            ingredientGroupJoin = root.join("ingredientGroup", JoinType.LEFT);
                        }
                        path = ingredientGroupJoin.get(isIngredientGroupField(field));
                    } else {
                        path = root.get(field);
                    }

                    Predicate predicate = null;

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

            // Always exclude soft-deleted records
            predicates.add(cb.isFalse(root.get("deleted")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
