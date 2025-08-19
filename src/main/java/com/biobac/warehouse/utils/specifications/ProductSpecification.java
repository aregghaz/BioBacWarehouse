package com.biobac.warehouse.utils.specifications;

import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.request.FilterCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductSpecification {
    public static Specification<Product> buildSpecification(Map<String, FilterCriteria> filters) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (filters != null) {
                for (Map.Entry<String, FilterCriteria> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    Predicate predicate = null;

                    switch (criteria.getOperator()) {
                        case "equals" -> {
                            predicate = cb.equal(root.get(field), criteria.getValue());
                        }
                        case "contains" -> {
                            predicate = cb.like(cb.lower(root.get(field)), "%" + criteria.getValue().toString().toLowerCase() + "%");
                        }
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
