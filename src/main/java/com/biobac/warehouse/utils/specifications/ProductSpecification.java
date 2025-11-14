package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.ProductGroup;
import com.biobac.warehouse.entity.RecipeItem;
import com.biobac.warehouse.entity.Unit;
import com.biobac.warehouse.request.FilterCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.biobac.warehouse.utils.SpecificationUtil.*;

public class ProductSpecification {

    private static String isRecipeItemField(String field) {
        Map<String, String> recipeItemField = Map.of(
                "recipeItemId", "id",
                "recipeItemName", "name"
        );
        return recipeItemField.getOrDefault(field, null);
    }

    private static String isUnitField(String field) {
        Map<String, String> unitField = Map.of(
                "unitId", "id",
                "unitName", "name"
        );
        return unitField.getOrDefault(field, null);
    }

    private static String isProductGroupField(String field) {
        Map<String, String> ingredientGroupField = Map.of(
                "productGroupId", "id",
                "productGroupName", "name"
        );
        return ingredientGroupField.getOrDefault(field, null);
    }

    public static Specification<Product> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Product, RecipeItem> recipeItemJoin = null;
            Join<Product, ProductGroup> productGroupJoin = null;
            Join<Product, Unit> unitJoin = null;

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Path<?> path;
                    Predicate predicate = null;

                    if (isRecipeItemField(field) != null) {
                        if (recipeItemJoin == null) {
                            recipeItemJoin = root.join("recipeItem", JoinType.LEFT);
                        }
                        path = recipeItemJoin.get(isRecipeItemField(field));
                    } else if (isProductGroupField(field) != null) {
                        if (productGroupJoin == null) {
                            productGroupJoin = root.join("productGroup", JoinType.LEFT);
                        }
                        path = productGroupJoin.get(isProductGroupField(field));
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

    public static Specification<Product> isDeleted() {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted")));
    }

    public static Specification<Product> belongsToGroups(List<Long> groupIds) {
        return (root, query, cb) -> {
            if (groupIds == null || groupIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("productGroup").get("id").in(groupIds);
        };
    }

    public static Specification<Product> containIds(List<Long> ids) {
        return (root, query, criteriaBuilder) -> {
            if (ids == null || ids.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(root.get("id"));
            for (Long id : ids) {
                inClause.value(id);
            }
            return inClause;
        };
    }
}
