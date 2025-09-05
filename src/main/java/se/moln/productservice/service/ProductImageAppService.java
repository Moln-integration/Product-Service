package se.moln.productservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.mappning.ProductMapper;
import se.moln.productservice.model.Product;
import se.moln.productservice.repository.ProductRepository;

import java.io.IOException;
import java.util.UUID;

@Service
public class ProductImageAppService {

    private final ProductRepository repo;
    private final ProductMapper mapper;
    private final FileStorageService storage;

    public ProductImageAppService(ProductRepository repo,
                                  ProductMapper mapper,
                                  FileStorageService storage) {
        this.repo = repo;
        this.mapper = mapper;
        this.storage = storage;
    }

    @Transactional
    public ProductResponse uploadImage(UUID productId, MultipartFile file) throws IOException {
        Product p = repo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        var stored = storage.store(file);
        p.setImageUrl(stored.url());

        var saved = repo.save(p);
        return mapper.toResponse(saved);
    }
}