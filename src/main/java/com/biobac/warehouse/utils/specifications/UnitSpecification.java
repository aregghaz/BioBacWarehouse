package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Unit;
import com.biobac.warehouse.entity.UnitType;
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

public class UnitSpecification {
    private static String isUnitTypeField(String field) {
        Map<String, String> unitTypeField = Map.of(
                "unitTypeId", "id",
                "unitTypeName", "name"
        );
        return unitTypeField.getOrDefault(field, null);
    }

    public static Specification<Unit> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Unit, UnitType> unitUnitTypeJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;

                    Path<?> path;

                    if (isUnitTypeField(field) != null) {
                        if (unitUnitTypeJoin == null) {
                            unitUnitTypeJoin = root.join("unitType", JoinType.LEFT);
                        }
                        path = unitUnitTypeJoin.get(isUnitTypeField(field));
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
