package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.RecipeItem;
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

public class RecipeItemSpecification {


    private static String isProductField(String field) {
        Map<String, String> productField = Map.of(
                "productId", "id",
                "productName", "name"
        );
        return productField.getOrDefault(field, null);
    }

    private static String isIngredientField(String field) {
        Map<String, String> ingredientField = Map.of(
                "ingredientId", "id",
                "ingredientName", "name"
        );
        return ingredientField.getOrDefault(field, null);
    }

    public static Specification<RecipeItem> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            Join<RecipeItem, Product> productJoin = null;
            Join<RecipeItem, Ingredient> ingredientJoin = null;

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
                    } else if (isIngredientField(field) != null) {
                        if (ingredientJoin == null) {
                            ingredientJoin = root.join("ingredient", JoinType.LEFT);
                        }
                        path = ingredientJoin.get(isIngredientField(field));
                    } else {
                        path = root.get(field);
                    }

                    switch (criteria.getOperator()) {
                        case "equals" -> predicate = buildEquals(cb, path, criteria.getValue());
                        case "notEquals" -> predicate = buildNotEquals(cb, path, criteria.getValue());
                        case "contains" -> predicate = cb.like(cb.lower(path.as(String.class)),
                                criteria.getValue().toString().toLowerCase().trim().replaceAll("\\s+", " "));
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
