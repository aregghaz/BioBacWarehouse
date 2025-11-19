package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.HistoryAction;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.ProductHistory;
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

public class ProductHistorySpecification {
    private static String isProductField(String field) {
        Map<String, String> productField = Map.of(
                "productId", "id",
                "productName", "name"
        );
        return productField.getOrDefault(field, null);
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

    public static Specification<ProductHistory> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            Join<ProductHistory, Product> productJoin = null;
            Join<ProductHistory, HistoryAction> actionJoin = null;
            Join<ProductHistory, Warehouse> warehouseJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;

                    Path<?> path;

                    if (isProductField(field) != null) {
                        if (productJoin == null) {
                            productJoin = root.join("product", JoinType.LEFT);
                        }
                        path = productJoin.get(isProductField(field));
                    } else if (isWarehouseField(field) != null) {
                        if (warehouseJoin == null) {
                            warehouseJoin = root.join("warehouse", JoinType.LEFT);
                        }
                        path = warehouseJoin.get(isWarehouseField(field));
                    } else if (isActionField(field) != null) {
                        if (actionJoin == null) {
                            actionJoin = root.join("action", JoinType.LEFT);
                        }
                        path = actionJoin.get(isActionField(field));
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

    public static Specification<ProductHistory> belongsToProductGroups(List<Long> groupIds) {
        return (root, query, cb) -> {
            if (groupIds == null || groupIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("product").get("productGroup").get("id").in(groupIds);
        };
    }
}