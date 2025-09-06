package se.moln.productservice.service;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import se.moln.productservice.model.Category;
import se.moln.productservice.model.Product;

import java.math.BigDecimal;

public class ProductSpecifications {

    public static Specification<Product> hasNameLike(String name) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
    }

    public static Specification<Product> hasCategoryName(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            Join<Product, Category> categoryJoin = root.join("category");
            return criteriaBuilder.equal(categoryJoin.get("name"), categoryName);
        };
    }

    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return ((root, query, criteriaBuilder) -> {
            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            }

            if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            }

            if (maxPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), maxPrice);
            }

            return criteriaBuilder.conjunction();
        });
    }


    public static Specification<Product> fetchAttributes() {
        return (root, query, criteriaBuilder) -> {
            root.fetch("attributes");
            return query.distinct(true).getRestriction();
        };
    }
}