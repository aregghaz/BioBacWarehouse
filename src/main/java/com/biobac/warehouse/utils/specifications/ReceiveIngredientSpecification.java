package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.request.FilterCriteria;
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

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    Path<?> path;
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;


                    path = root.get(field);


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
