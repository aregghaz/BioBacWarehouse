package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.ComponentType;
import com.biobac.warehouse.entity.Inventory;
import com.biobac.warehouse.request.FilterCriteria;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.biobac.warehouse.utils.SpecificationUtil.*;

public class InventorySpecification {
    public static Specification<Inventory> hasType(ComponentType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Inventory> buildSpecification(Map<String, FilterCriteria> filters, ComponentType type) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();

                    Path<?> path;
                    switch (field) {
                        case "warehouseId" -> path = root.get("warehouse").get("id");
                        case "warehouseName" -> path = root.get("warehouse").get("name");
                        default -> path = root.get(field);
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
                    if (predicate != null) predicates.add(predicate);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
