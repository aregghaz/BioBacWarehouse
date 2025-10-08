package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.utils.DateUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.biobac.warehouse.utils.SpecificationUtil.*;

public class ComponentBalanceSpecification {

    private static String isIngredientGroupField(String field) {
        Map<String, String> ingredientGroupField = Map.of(
                "ingredientGroupId", "id",
                "ingredientGroupName", "name"
        );
        return ingredientGroupField.getOrDefault(field, null);
    }

    private static String isProductGroupField(String field) {
        Map<String, String> productGroupField = Map.of(
                "productGroupId", "id",
                "productGroupName", "name"
        );
        return productGroupField.getOrDefault(field, null);
    }

    private static String isProductField(String field) {
        Map<String, String> productField = Map.of(
                "productId", "id",
                "productName", "name",
                "minimalBalance", "minimalBalance"
        );
        return productField.getOrDefault(field, null);
    }

    private static String isIngredientField(String field) {
        Map<String, String> ingredientField = Map.of(
                "ingredientId", "id",
                "ingredientName", "name",
                "minimalBalance", "minimalBalance"
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

    public static Specification<ComponentBalance> buildSpecification(Map<String, FilterCriteria> filters, String type) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<ComponentBalance, Ingredient> balanceIngredientJoin = null;
            Join<ComponentBalance, Product> balanceProductJoin = null;
            Join<ComponentBalance, Warehouse> balanceWarehouseJoin = null;
            Join<Ingredient, IngredientGroup> ingredientGroupJoin = null;
            Join<Product, ProductGroup> productGroupJoin = null;
            Join<Product, InventoryItem> productInventoryItemJoin = null;
            Join<Ingredient, InventoryItem> ingredientInventoryItemJoin = null;

            List<Predicate> predicates = new ArrayList<>();

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    // inventoryItems removed: skip expiration-date based filters tied to inventory batches
                    if ("expirationDate".equals(field) || "localDate".equals(field)) {
                        continue;
                    }
                    Path<?> path = null;
                    Object effectiveValue = criteria.getValue();

                    if (Objects.equals(type, "product")) {
                        if (isProductField(field) != null) {
                            if (balanceProductJoin == null) {
                                balanceProductJoin = root.join("product", JoinType.LEFT);
                            }
                            path = balanceProductJoin.get(isProductField(field));
                        } else if (isProductGroupField(field) != null) {
                            if (balanceProductJoin == null) {
                                balanceProductJoin = root.join("product", JoinType.LEFT);
                            }
                            if (productGroupJoin == null) {
                                productGroupJoin = balanceProductJoin.join("productGroup", JoinType.LEFT);
                            }
                            path = productGroupJoin.get(isProductGroupField(field));
                        } else if (isWarehouseField(field) != null) {
                            if (balanceWarehouseJoin == null) {
                                balanceWarehouseJoin = root.join("warehouse", JoinType.LEFT);
                            }
                            path = balanceWarehouseJoin.get(isWarehouseField(field));
                        } else if ("expirationDate".equals(field) || "localDate".equals(field)) {
                            if (balanceProductJoin == null) {
                                balanceProductJoin = root.join("product", JoinType.LEFT);
                            }
                            if (productInventoryItemJoin == null) {
                                productInventoryItemJoin = balanceProductJoin.join("inventoryItems", JoinType.LEFT);
                            }
                            path = productInventoryItemJoin.get("expirationDate");
                            if ("localDate".equals(field)) {
                                effectiveValue = LocalDate.now()
                                        .format(DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT));
                            }
                        }
                    } else if (Objects.equals(type, "ingredient")) {
                        if (isIngredientField(field) != null) {
                            if (balanceIngredientJoin == null) {
                                balanceIngredientJoin = root.join("ingredient", JoinType.LEFT);
                            }
                            path = balanceIngredientJoin.get(isIngredientField(field));
                        } else if (isIngredientGroupField(field) != null) {
                            if (balanceIngredientJoin == null) {
                                balanceIngredientJoin = root.join("ingredient", JoinType.LEFT);
                            }
                            if (ingredientGroupJoin == null) {
                                ingredientGroupJoin = balanceIngredientJoin.join("ingredientGroup", JoinType.LEFT);
                            }
                            path = ingredientGroupJoin.get(isIngredientGroupField(field));
                        } else if (isWarehouseField(field) != null) {
                            if (balanceWarehouseJoin == null) {
                                balanceWarehouseJoin = root.join("warehouse", JoinType.LEFT);
                            }
                            path = balanceWarehouseJoin.get(isWarehouseField(field));
                        } else if ("expirationDate".equals(field) || "localDate".equals(field)) {
                            if (balanceIngredientJoin == null) {
                                balanceIngredientJoin = root.join("ingredient", JoinType.LEFT);
                            }
                            if (ingredientInventoryItemJoin == null) {
                                ingredientInventoryItemJoin = balanceIngredientJoin.join("inventoryItems", JoinType.LEFT);
                            }
                            path = ingredientInventoryItemJoin.get("expirationDate");
                            if ("localDate".equals(field)) {
                                effectiveValue = LocalDate.now()
                                        .format(DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT));
                            }
                        }
                    }

                    if (path == null) {
                        path = root.get(field);
                    }

                    Predicate predicate = null;

                    switch (criteria.getOperator()) {
                        case "equals" -> predicate = buildEquals(cb, path, effectiveValue);
                        case "notEquals" -> predicate = buildNotEquals(cb, path, effectiveValue);
                        case "contains" -> predicate = buildContains(cb, path, effectiveValue);
                        case "greaterThanOrEqualTo" -> predicate = buildGreaterThanOrEqualTo(cb, path, effectiveValue);
                        case "lessThanOrEqualTo" -> predicate = buildLessThanOrEqualTo(cb, path, effectiveValue);
                        case "between" -> predicate = buildBetween(cb, path, effectiveValue);
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
