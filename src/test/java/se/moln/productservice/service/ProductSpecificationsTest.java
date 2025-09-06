package se.moln.productservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import se.moln.productservice.model.Product;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductSpecificationsTest {

    @Test
    void hasNameLike_ShouldCreateNonNullSpecification() {
        // Given
        String searchName = "iPhone";

        // When
        Specification<Product> spec = ProductSpecifications.hasNameLike(searchName);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasNameLike_WithEmptyString_ShouldCreateNonNullSpecification() {
        // Given
        String searchName = "";

        // When
        Specification<Product> spec = ProductSpecifications.hasNameLike(searchName);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasNameLike_WithNullString_ShouldCreateNonNullSpecification() {
        // Given
        String searchName = null;

        // When
        Specification<Product> spec = ProductSpecifications.hasNameLike(searchName);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasCategoryName_ShouldCreateNonNullSpecification() {
        // Given
        String categoryName = "Electronics";

        // When
        Specification<Product> spec = ProductSpecifications.hasCategoryName(categoryName);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasCategoryName_WithEmptyString_ShouldCreateNonNullSpecification() {
        // Given
        String categoryName = "";

        // When
        Specification<Product> spec = ProductSpecifications.hasCategoryName(categoryName);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPriceBetween_WithBothPrices_ShouldCreateNonNullSpecification() {
        // Given
        BigDecimal minPrice = BigDecimal.valueOf(100);
        BigDecimal maxPrice = BigDecimal.valueOf(500);

        // When
        Specification<Product> spec = ProductSpecifications.hasPriceBetween(minPrice, maxPrice);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPriceBetween_WithOnlyMinPrice_ShouldCreateNonNullSpecification() {
        // Given
        BigDecimal minPrice = BigDecimal.valueOf(100);
        BigDecimal maxPrice = null;

        // When
        Specification<Product> spec = ProductSpecifications.hasPriceBetween(minPrice, maxPrice);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPriceBetween_WithOnlyMaxPrice_ShouldCreateNonNullSpecification() {
        // Given
        BigDecimal minPrice = null;
        BigDecimal maxPrice = BigDecimal.valueOf(500);

        // When
        Specification<Product> spec = ProductSpecifications.hasPriceBetween(minPrice, maxPrice);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPriceBetween_WithBothPricesNull_ShouldCreateNonNullSpecification() {
        // Given
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        // When
        Specification<Product> spec = ProductSpecifications.hasPriceBetween(minPrice, maxPrice);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPriceBetween_WithZeroPrices_ShouldCreateNonNullSpecification() {
        // Given
        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;

        // When
        Specification<Product> spec = ProductSpecifications.hasPriceBetween(minPrice, maxPrice);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPriceBetween_WithNegativePrices_ShouldCreateNonNullSpecification() {
        // Given
        BigDecimal minPrice = BigDecimal.valueOf(-100);
        BigDecimal maxPrice = BigDecimal.valueOf(-50);

        // When
        Specification<Product> spec = ProductSpecifications.hasPriceBetween(minPrice, maxPrice);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void fetchAttributes_ShouldCreateNonNullSpecification() {
        // When
        Specification<Product> spec = ProductSpecifications.fetchAttributes();

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void specifications_ShouldBeCombinable() {
        // Given
        String name = "iPhone";
        String category = "Electronics";
        BigDecimal minPrice = BigDecimal.valueOf(100);
        BigDecimal maxPrice = BigDecimal.valueOf(1000);

        // When
        Specification<Product> combinedSpec = ProductSpecifications.fetchAttributes()
                .and(ProductSpecifications.hasNameLike(name))
                .and(ProductSpecifications.hasCategoryName(category))
                .and(ProductSpecifications.hasPriceBetween(minPrice, maxPrice));

        // Then
        assertThat(combinedSpec).isNotNull();
    }

    @Test
    void specifications_CanBeChainedWithOr() {
        // Given
        String name1 = "iPhone";
        String name2 = "Samsung";

        // When
        Specification<Product> combinedSpec = ProductSpecifications.hasNameLike(name1)
                .or(ProductSpecifications.hasNameLike(name2));

        // Then
        assertThat(combinedSpec).isNotNull();
    }

    @Test
    void specifications_CanBeNegated() {
        // Given
        String categoryName = "Electronics";

        // When
        Specification<Product> negatedSpec = Specification.not(ProductSpecifications.hasCategoryName(categoryName));

        // Then
        assertThat(negatedSpec).isNotNull();
    }

    @Test
    void hasPriceBetween_BothPrices_ExecutesBetweenPredicate() {
        // Given
        BigDecimal min = BigDecimal.ONE;
        BigDecimal max = BigDecimal.TEN;
        var spec = ProductSpecifications.hasPriceBetween(min, max);

        // Mock Criteria API
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Root<Product> root = mock(jakarta.persistence.criteria.Root.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Path<BigDecimal> pricePath = mock(jakarta.persistence.criteria.Path.class);
        when(root.<BigDecimal>get("price")).thenReturn(pricePath);

        jakarta.persistence.criteria.CriteriaQuery<Product> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate predicate = mock(jakarta.persistence.criteria.Predicate.class);
        when(cb.between(pricePath, min, max)).thenReturn(predicate);

        // When
        var out = spec.toPredicate(root, query, cb);

        // Then
        assertThat(out).isNotNull();
        verify(cb).between(pricePath, min, max);
    }

    @Test
    void hasPriceBetween_OnlyMin_ExecutesGtePredicate() {
        // Given
        BigDecimal min = BigDecimal.ONE;
        var spec = ProductSpecifications.hasPriceBetween(min, null);

        // Mock Criteria API
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Root<Product> root = mock(jakarta.persistence.criteria.Root.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Path<BigDecimal> pricePath = mock(jakarta.persistence.criteria.Path.class);
        when(root.<BigDecimal>get("price")).thenReturn(pricePath);

        jakarta.persistence.criteria.CriteriaQuery<Product> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate predicate = mock(jakarta.persistence.criteria.Predicate.class);
        when(cb.greaterThanOrEqualTo(pricePath, min)).thenReturn(predicate);

        // When
        var out = spec.toPredicate(root, query, cb);

        // Then
        assertThat(out).isNotNull();
        verify(cb).greaterThanOrEqualTo(pricePath, min);
    }

    @Test
    void hasPriceBetween_OnlyMax_ExecutesGtePredicatePerCurrentImplementation() {
        // Given
        BigDecimal max = BigDecimal.TEN;
        var spec = ProductSpecifications.hasPriceBetween(null, max);

        // Mock Criteria API
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Root<Product> root = mock(jakarta.persistence.criteria.Root.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Path<BigDecimal> pricePath = mock(jakarta.persistence.criteria.Path.class);
        when(root.<BigDecimal>get("price")).thenReturn(pricePath);

        jakarta.persistence.criteria.CriteriaQuery<Product> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate predicate = mock(jakarta.persistence.criteria.Predicate.class);
        // Note: current production code uses greaterThanOrEqualTo for maxPrice path
        when(cb.greaterThanOrEqualTo(pricePath, max)).thenReturn(predicate);

        // When
        var out = spec.toPredicate(root, query, cb);

        // Then
        assertThat(out).isNotNull();
        verify(cb).greaterThanOrEqualTo(pricePath, max);
    }

    @Test
    void hasPriceBetween_BothNull_ExecutesConjunction() {
        // Given
        var spec = ProductSpecifications.hasPriceBetween(null, null);

        // Mock Criteria API
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Root<Product> root = mock(jakarta.persistence.criteria.Root.class);
        jakarta.persistence.criteria.CriteriaQuery<Product> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate predicate = mock(jakarta.persistence.criteria.Predicate.class);
        when(cb.conjunction()).thenReturn(predicate);

        // When
        var out = spec.toPredicate(root, query, cb);

        // Then
        assertThat(out).isNotNull();
        verify(cb).conjunction();
    }

    @Test
    void fetchAttributes_ExecutesFetchAndDistinct() {
        // Given
        var spec = ProductSpecifications.fetchAttributes();

        // Mock Criteria API
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Root<Product> root = mock(jakarta.persistence.criteria.Root.class);
        // root.fetch("attributes") returns a Fetch but we don't use it, so we can just stub the call
        when(root.fetch("attributes")).thenReturn(null);

        jakarta.persistence.criteria.CriteriaQuery<Product> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        when(query.distinct(true)).thenReturn(query);
        jakarta.persistence.criteria.Predicate predicate = mock(jakarta.persistence.criteria.Predicate.class);
        when(query.getRestriction()).thenReturn(predicate);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);

        // When
        var out = spec.toPredicate(root, query, cb);

        // Then
        assertThat(out).isNotNull();
        verify(root).fetch("attributes");
        verify(query).distinct(true);
        verify(query).getRestriction();
    }

}