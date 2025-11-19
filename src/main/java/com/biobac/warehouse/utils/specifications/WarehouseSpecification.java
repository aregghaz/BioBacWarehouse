package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.entity.WarehouseGroup;
import com.biobac.warehouse.entity.WarehouseType;
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

public class WarehouseSpecification {

    private static String isTypeField(String field) {
        Map<String, String> typeField = Map.of(
                "warehouseTypeId", "id"
        );
        return typeField.getOrDefault(field, null);
    }

    private static String isGroupField(String field) {
        Map<String, String> groupField = Map.of(
                "warehouseGroupId", "id"
        );
        return groupField.getOrDefault(field, null);
    }

    public static Specification<Warehouse> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Warehouse, WarehouseType> warehouseTypeJoin = null;
            Join<Warehouse, WarehouseGroup> warehouseGroupJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    Path<?> path;
                    if (isTypeField(field) != null) {
                        if (warehouseTypeJoin == null) {
                            warehouseTypeJoin = root.join("warehouseType", JoinType.LEFT);
                        }
                        path = warehouseTypeJoin.get(isTypeField(field));
                    } else if (isGroupField(field) != null) {
                        if (warehouseGroupJoin == null) {
                            warehouseGroupJoin = root.join("warehouseGroup", JoinType.LEFT);
                        }
                        path = warehouseGroupJoin.get(isGroupField(field));
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

    public static Specification<Warehouse> isDeleted() {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted")));
    }

    public static Specification<Warehouse> belongsToGroups(List<Long> groupIds) {
        return (root, query, cb) -> {
            if (groupIds == null || groupIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("warehouseGroup").get("id").in(groupIds);
        };
    }
}