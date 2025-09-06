package se.moln.productservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.mappning.ProductMapper;
import se.moln.productservice.model.Product;
import se.moln.productservice.repository.ProductRepository;

import java.util.UUID;

@Service
public class ProductReadService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    public ProductReadService(ProductRepository repo, ProductMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public ProductResponse getById(UUID id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        return mapper.toResponse(p);
    }
}