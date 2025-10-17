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

public class ProductBalanceSpecification {
    private static String isProductField(String field) {
        Map<String, String> productField = Map.of(
                "productId", "id",
                "productName", "name"
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

    public static Specification<ProductBalance> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<ManufactureProduct, Product> productJoin = null;
            Join<ManufactureProduct, Warehouse> warehouseJoin = null;
            Join<ManufactureProduct, Unit> unitJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    Path<?> path;
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;

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
