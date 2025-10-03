package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.request.FilterCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static Specification<ComponentBalance> buildSpecification(Map<String, FilterCriteria> filters, String type) {

        Join<ComponentBalance, Ingredient> balanceIngredientJoin = null;
        Join<ComponentBalance, Product> balanceProductJoin = null;
        Join<Ingredient, InventoryItem> ingredientInventoryItemJoin = null;
        Join<Product, InventoryItem> productInventoryItemJoin = null;
        Join<ComponentBalance, Warehouse> balanceWarehouseJoin = null;
        Join<Ingredient, IngredientGroup> ingredientGroupJoin = null;
        Join<Product, ProductGroup> productGroupJoin = null;

        return (root, query, cb) -> {
//            if (Objects.equals(type, "product")) {
//                Join<ComponentBalance, Product> balanceProductJoin = null;
//                Join<Product, InventoryItem> productInventoryItemJoin = null;
//                Join<Product, ProductGroup> productGroupJoin = null;
//            } else {
//                Join<ComponentBalance, Ingredient> balanceIngredientJoin = null;
//                Join<Ingredient, InventoryItem> ingredientInventoryItemJoin = null;
//                Join<Ingredient, IngredientGroup> ingredientGroupJoin = null;
//            }
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Path<?> path = root.get(field);
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
}
