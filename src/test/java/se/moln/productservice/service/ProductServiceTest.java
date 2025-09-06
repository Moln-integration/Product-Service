package se.moln.productservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.exception.DuplicateProductException;
import se.moln.productservice.mappning.ProductMapper;
import se.moln.productservice.model.Category;
import se.moln.productservice.model.Product;
import se.moln.productservice.repository.CategoryRepository;
import se.moln.productservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private Product product;
    private ProductResponse productResponse;
    private Category category;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");

        productRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(99.99),
                "SEK",
                categoryId,
                "Electronics",
                10,
                new HashMap<>(),
                Arrays.asList("http://example.com/image.jpg")
        );

        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setCategory(category);

        productResponse = new ProductResponse(
                productId,
                "Test Product",
                "test-product",
                "Test Description",
                BigDecimal.valueOf(99.99),
                "SEK",
                "Electronics",
                10,
                true,
                new HashMap<>(),
                Arrays.asList("http://example.com/image.jpg")
        );
    }

    @Test
    void create_WithValidData_ShouldReturnProductResponse() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        ProductResponse result = productService.create(productRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Product");
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void create_WithDuplicateSlug_ShouldThrowDuplicateProductException() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessageContaining("Slug already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WithDuplicateName_ShouldThrowDuplicateProductException() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessageContaining("Product name already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WithNewCategoryName_ShouldCreateCategory() {
        ProductRequest requestWithNewCategory = new ProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(99.99),
                "SEK",
                null,
                "New Category",
                10,
                new HashMap<>(),
                Collections.emptyList()
        );

        Category newCategory = new Category();
        newCategory.setName("New Category");
        newCategory.setSlug("new-category");

        when(categoryRepository.findByNameIgnoreCase("New Category")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        ProductResponse result = productService.create(requestWithNewCategory);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(productRepository).save(product);
    }

    @Test
    void create_WithoutCategoryInfo_ShouldUseUncategorized() {
        ProductRequest requestWithoutCategory = new ProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(99.99),
                "SEK",
                null,
                null,
                10,
                new HashMap<>(),
                Collections.emptyList()
        );

        Category uncategorized = new Category();
        uncategorized.setName("Uncategorized");
        uncategorized.setSlug("uncategorized");

        when(categoryRepository.findByNameIgnoreCase("Uncategorized")).thenReturn(Optional.of(uncategorized));
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        ProductResponse result = productService.create(requestWithoutCategory);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).findByNameIgnoreCase("Uncategorized");
        verify(productRepository).save(product);
    }

    @Test
    void getAllProducts_ShouldReturnPagedProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAllWithAttributes(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Product");
        verify(productRepository).findAllWithAttributes(pageable);
    }

    @Test
    void getAllProductsWithoutPagination_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAllWithAttributes()).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        List<ProductResponse> result = productService.getAllProductsWithoutPagination();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
        verify(productRepository).findAllWithAttributes();
    }

    @Test
    void searchProducts_WithAllParameters_ShouldReturnFilteredProducts() {
        // Given
        String name = "Test";
        String categoryName = "Electronics";
        BigDecimal minPrice = BigDecimal.valueOf(50);
        BigDecimal maxPrice = BigDecimal.valueOf(150);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        List<ProductResponse> result = productService.searchProducts(name, categoryName, minPrice, maxPrice);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void searchProducts_WithNullParameters_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll(any(Specification.class))).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When
        List<ProductResponse> result = productService.searchProducts(null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void create_WithInvalidCategoryId_ShouldThrowIllegalArgumentException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WithExistingCategoryName_ShouldUseExistingCategory() {
        ProductRequest req = new ProductRequest(
                "Name",
                "Desc",
                BigDecimal.TEN,
                "SEK",
                null,
                "Electronics",
                0,
                new HashMap<>(),
                Collections.emptyList()
        );

        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse out = productService.create(req);

        assertThat(out).isNotNull();
        verify(categoryRepository, never()).save(any());
        verify(productRepository).save(product);
    }

    @Test
    void create_NoCategory_UncategorizedMissing_ShouldCreateUncategorized() {
        ProductRequest req = new ProductRequest(
                "Name",
                "Desc",
                BigDecimal.TEN,
                "SEK",
                null,
                null,
                0,
                new HashMap<>(),
                Collections.emptyList()
        );

        when(categoryRepository.findByNameIgnoreCase("Uncategorized")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.toEntity(any(ProductRequest.class), any(Category.class))).thenReturn(product);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse out = productService.create(req);

        assertThat(out).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(productRepository).save(product);
    }

    @Test
    void searchProducts_WithOnlyName_ShouldFilter() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        List<ProductResponse> result = productService.searchProducts("Test", null, null, null);

        assertThat(result).hasSize(1);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void searchProducts_WithOnlyCategoryName_ShouldFilter() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        List<ProductResponse> result = productService.searchProducts(null, "Electronics", null, null);

        assertThat(result).hasSize(1);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void searchProducts_WithOnlyMinPrice_ShouldFilter() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        List<ProductResponse> result = productService.searchProducts(null, null, BigDecimal.ONE, null);

        assertThat(result).hasSize(1);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void searchProducts_WithOnlyMaxPrice_ShouldFilter() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        List<ProductResponse> result = productService.searchProducts(null, null, null, BigDecimal.TEN);

        assertThat(result).hasSize(1);
        verify(productRepository).findAll(any(Specification.class));
    }
}