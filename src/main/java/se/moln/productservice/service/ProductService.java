package se.moln.productservice.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.exception.DuplicateProductException;
import se.moln.productservice.exception.ResourceNotFoundException;
import se.moln.productservice.mappning.ProductMapper;
import se.moln.productservice.model.Category;
import se.moln.productservice.model.Product;
import se.moln.productservice.repository.CategoryRepository;
import se.moln.productservice.repository.ProductRepository;
import static se.moln.productservice.service.ProductSpecifications.*;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final CategoryRepository catRepo;
    private final ProductMapper mapper;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository repo, CategoryRepository catRepo, ProductMapper mapper, FileStorageService fileStorageService){
        this.repo = repo;
        this.catRepo = catRepo;
        this.mapper = mapper;
        this.fileStorageService = fileStorageService;
    }


    @Transactional
    public ProductResponse create(ProductRequest req, MultipartFile file) throws IOException {
        System.out.println("service");

        Category category = resolveCategory(req.categoryId(), req.categoryName());
        Product entity = mapper.toEntity(req, category);

        if (repo.existsBySlug(entity.getSlug())) {
            throw new DuplicateProductException("Slug already exists: " + entity.getSlug());
        }

        if (repo.existsByNameIgnoreCase(entity.getName())) {
            throw new DuplicateProductException("Product name already exists: " + entity.getName());
        }




        if (file != null && !file.isEmpty()) {
            FileStorageService.StoredFile storedFile = fileStorageService.store(file);
            entity.setImageUrl(storedFile.url());
        }

        Product savedProduct = repo.save(entity);

        return mapper.toResponse(savedProduct);
    }


    @Transactional
    public Page<ProductResponse> getAllProducts(Pageable pageable){
        Page<Product> productPage = repo.findAllWithAttributes(pageable);
        return productPage.map(mapper::toResponse);
    }

    @Transactional
    public List<ProductResponse> getAllProductsWithoutPagination(){
        List<Product> products = repo.findAllWithAttributes();

        return products.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductResponse> searchProducts(String name, String categoryName, BigDecimal minPrice, BigDecimal maxPrice){
        Specification<Product> combinedSpec = fetchAttributes();;

        if (name !=null){
            combinedSpec = combinedSpec.and(hasNameLike(name));
        }

        if (categoryName != null){
            combinedSpec = combinedSpec.and(hasCategoryName(categoryName));
        }

        combinedSpec = combinedSpec.and(hasPriceBetween(minPrice, maxPrice));

        List<Product> products = repo.findAll(combinedSpec);

        return products.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private Category resolveCategory(UUID categoryId, String categoryName){
        if (categoryId != null){
            return catRepo.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        }

        if (categoryName !=null && !categoryName.isBlank()){
            String normalized = categoryName.trim();
            return catRepo.findByNameIgnoreCase(normalized)
                    .orElseGet(() -> {
                        Category c = new Category();
                        c.setName(normalized);
                        c.setSlug(slugify(normalized));
                        return catRepo.save(c);
                    });
        }

        return catRepo.findByNameIgnoreCase("Uncategorized")
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("Uncategorized");
                    c.setSlug("Uncategorized");
                    return catRepo.save(c);
                });
    }


    @Transactional
    public ProductResponse update(UUID id, ProductRequest req){
        Product productToUpdate = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found."));

        productToUpdate.setName(req.name());
        productToUpdate.setSlug(slugify(req.name()));
        productToUpdate.setDescription(req.description());
        productToUpdate.setPrice(req.price());


        Product updatedProduct = repo.save(productToUpdate);

        return mapper.toResponse(updatedProduct);
    }

    private String slugify(String s){
        return s.toLowerCase().trim().replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");
    }
}
