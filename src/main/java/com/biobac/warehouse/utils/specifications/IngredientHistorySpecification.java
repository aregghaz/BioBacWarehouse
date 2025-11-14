package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
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

public class IngredientHistorySpecification {
    private static String isIngredientField(String field) {
        Map<String, String> ingredientField = Map.of(
                "ingredientId", "id",
                "ingredientName", "name"
        );
        return ingredientField.getOrDefault(field, null);
    }

    private static String isWarehouseField(String field) {
        Map<String, String> warehouseField = Map.of(
                "warehouseId", "id",
                "warehouseName", "name"
        );
        return warehouseField.getOrDefault(field, null);
    }

    private static String isActionField(String field) {
        Map<String, String> actionField = Map.of(
                "actionId", "id",
                "actionName", "name"
        );
        return actionField.getOrDefault(field, null);
    }

    public static Specification<IngredientHistory> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<IngredientHistory, Ingredient> ingredientJoin = null;
            Join<IngredientHistory, Warehouse> warehouseJoin = null;
            Join<IngredientHistory, HistoryAction> actionJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;

                    Path<?> path;

                    if (isIngredientField(field) != null) {
                        if (ingredientJoin == null) {
                            ingredientJoin = root.join("ingredient", JoinType.LEFT);
                        }
                        path = ingredientJoin.get(isIngredientField(field));
                    } else if (isActionField(field) != null) {
                        if (actionJoin == null) {
                            actionJoin = root.join("action", JoinType.LEFT);
                        }
                        path = actionJoin.get(isActionField(field));
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

    public static Specification<IngredientHistory> belongsToIngredientGroups(List<Long> groupIds) {
        return (root, query, cb) -> {
            if (groupIds == null || groupIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("ingredient").get("ingredientGroup").get("id").in(groupIds);
        };
    }
}
