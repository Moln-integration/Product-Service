package se.moln.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import se.moln.productservice.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategory_IdAndActiveTrue(UUID categoryId, Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.attributes")
    Page<Product> findAllWithAttributes(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.attributes")
    List<Product> findAllWithAttributes();
}