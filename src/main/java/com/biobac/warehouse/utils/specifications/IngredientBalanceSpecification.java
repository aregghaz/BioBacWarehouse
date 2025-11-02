package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.entity.Warehouse;
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

public class IngredientBalanceSpecification {
    private static String isIngredientField(String field) {
        Map<String, String> productField = Map.of(
                "ingredientId", "id",
                "ingredientMinimalBalance", "minimalBalance"
        );
        return productField.getOrDefault(field, null);
    }

    private static String isIngredientGroupField(String field) {
        Map<String, String> productField = Map.of(
                "ingredientGroupId", "id"
        );
        return productField.getOrDefault(field, null);
    }

    private static String isUnitField(String field) {
        Map<String, String> unitField = Map.of(
                "unitId", "id",
                "unitName", "name"
        );
        return unitField.getOrDefault(field, null);
    }

    private static String isWarehouseField(String field) {
        Map<String, String> warehouseField = Map.of(
                "warehouseId", "id",
                "warehouseName", "name"
        );
        return warehouseField.getOrDefault(field, null);
    }

    public static Specification<IngredientBalance> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<IngredientBalance, Ingredient> ingredientJoin = null;
            Join<IngredientBalance, Warehouse> warehouseJoin = null;
            Join<Ingredient, IngredientGroup> ingredientGroupJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    Path<?> path;
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;

                    if (isIngredientField(field) != null) {
                        if (ingredientJoin == null) {
                            ingredientJoin = root.join("ingredient", JoinType.LEFT);
                        }
                        path = ingredientJoin.get(isIngredientField(field));
                    } else if (isIngredientGroupField(field) != null) {
                        if (ingredientGroupJoin == null) {
                            ingredientGroupJoin = root.join("ingredient", JoinType.LEFT).join("ingredientGroup", JoinType.LEFT);
                        }
                        path = ingredientGroupJoin.get(isIngredientGroupField(field));
                    } else if (isWarehouseField(field) != null) {
                        if (warehouseJoin == null) {
                            warehouseJoin = root.join("warehouse", JoinType.LEFT);
                        }
                        path = warehouseJoin.get(isWarehouseField(field));
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
