package se.moln.productservice.service;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.mappning.ProductMapper;
import se.moln.productservice.model.Product;
import se.moln.productservice.repository.ProductRepository;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    public ProductQueryService(ProductRepository repo, ProductMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public ProductResponse getById(UUID id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (p.getCategory() != null) p.getCategory().getName();
        Hibernate.initialize(p.getImages());
        Hibernate.initialize(p.getAttributes());

        return mapper.toResponse(p);
    }

    public Page<ProductResponse> listActive(int page, int size) {
        var result = repo.findByActiveTrue(PageRequest.of(page, size));
        initializeLazyForPage(result);
        return result.map(mapper::toResponse);
    }

    public Page<ProductResponse> listByCategory(UUID categoryId, int page, int size) {
        var result = repo.findByCategory_IdAndActiveTrue(categoryId, PageRequest.of(page, size));
        initializeLazyForPage(result);
        return result.map(mapper::toResponse);
    }

    private void initializeLazyForPage(Page<Product> page) {
        page.getContent().forEach(p -> {
            if (p.getCategory() != null) p.getCategory().getName();
            Hibernate.initialize(p.getImages());
            Hibernate.initialize(p.getAttributes());
        });
    }
}
