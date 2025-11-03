package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.*;
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

public class ReceiveIngredientSpecification {

    private static String isUnitField(String field) {
        Map<String, String> unitField = Map.of(
                "unitId", "id",
                "unitName", "name"
        );
        return unitField.getOrDefault(field, null);
    }

    private static String isGroupField(String field) {
        Map<String, String> groupField = Map.of(
                "groupId", "id"
        );
        return groupField.getOrDefault(field, null);
    }

    private static String isStatusField(String field) {
        Map<String, String> statusField = Map.of(
                "statusId", "id"
        );
        return statusField.getOrDefault(field, null);
    }

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

    public static Specification<ReceiveIngredient> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<ReceiveIngredient, Warehouse> receiveIngredientWarehouseJoin = null;
            Join<ReceiveIngredient, Ingredient> receiveIngredientIngredientJoin = null;
            Join<ReceiveIngredient, ReceiveIngredientStatus> receiveIngredientStatusJoin = null;
            Join<ReceiveIngredient, ReceiveGroup> receiveIngredientReceiveGroupJoin = null;
            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    Path<?> path;
                    if (isWarehouseField(field) != null) {
                        if (receiveIngredientWarehouseJoin == null) {
                            receiveIngredientWarehouseJoin = root.join("warehouse", JoinType.LEFT);
                        }
                        path = receiveIngredientWarehouseJoin.get(isWarehouseField(field));
                    } else if (isGroupField(field) != null) {
                        if (receiveIngredientReceiveGroupJoin == null) {
                            receiveIngredientReceiveGroupJoin = root.join("group", JoinType.LEFT);
                        }
                        path = receiveIngredientReceiveGroupJoin.get(isGroupField(field));
                    } else if (isIngredientField(field) != null) {
                        if (receiveIngredientIngredientJoin == null) {
                            receiveIngredientIngredientJoin = root.join("ingredient", JoinType.LEFT);
                        }
                        path = receiveIngredientIngredientJoin.get(isIngredientField(field));
                    } else if (isStatusField(field) != null) {
                        if (receiveIngredientStatusJoin == null) {
                            receiveIngredientStatusJoin = root.join("status", JoinType.LEFT);
                        }
                        path = receiveIngredientStatusJoin.get(isStatusField(field));
                    } else {
                        path = root.get(field);
                    }
                    FilterCriteria criteria = entry.getValue();
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ReceiveIngredient> belongsToWarehouseGroups(List<Long> groupIds) {
        return (root, query, cb) -> {
            if (groupIds == null || groupIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("warehouse").get("warehouseGroup").get("id").in(groupIds);
        };
    }

    public static Specification<ReceiveIngredient> belongsToIngredientGroups(List<Long> groupIds) {
        return (root, query, cb) -> {
            if (groupIds == null || groupIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("ingredient").get("ingredientGroup").get("id").in(groupIds);
        };
    }
}
