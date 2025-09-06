package se.moln.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import se.moln.productservice.dto.PageResponse;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.service.ProductImageAppService;
import se.moln.productservice.service.ProductQueryService;
import se.moln.productservice.service.ProductReadService;
import se.moln.productservice.service.ProductService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductImageAppService productImageAppService;

    @MockitoBean
    private ProductReadService productReadService;

    @MockitoBean
    private ProductQueryService productQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(99.99),
                "SEK",
                null,
                "Electronics",
                10,
                new HashMap<>(),
                Arrays.asList("http://example.com/image.jpg")
        );
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
    void create_ShouldReturnCreatedProduct() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    void getAllProducts_ShouldReturnPagedProducts() throws Exception {
        List<ProductResponse> products = Arrays.asList(productResponse);
        Page<ProductResponse> productPage = new PageImpl<>(products, PageRequest.of(0, 5), 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllProductsWithoutPagination_ShouldReturnAllProducts() throws Exception {
        List<ProductResponse> products = Arrays.asList(productResponse);
        when(productService.getAllProductsWithoutPagination()).thenReturn(products);

        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void searchProducts_ShouldReturnFilteredProducts() throws Exception {
        List<ProductResponse> products = Arrays.asList(productResponse);
        when(productService.searchProducts(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(products);

        mockMvc.perform(get("/api/products/search")
                        .param("name", "Test")
                        .param("categoryName", "Electronics")
                        .param("minPrice", "50.00")
                        .param("maxPrice", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void searchProducts_WithoutParameters_ShouldReturnAllProducts() throws Exception {
        List<ProductResponse> products = Arrays.asList(productResponse);
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(products);

        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void uploadImage_ShouldReturnUpdatedProduct() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(productImageAppService.uploadImage(eq(productId), any())).thenReturn(productResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/{id}/images", productId)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()));
    }

    @Test
    void create_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "", // Empty name should fail validation
                "Test Description",
                BigDecimal.valueOf(99.99),
                "SEK",
                null,
                "Electronics",
                10,
                new HashMap<>(),
                Arrays.asList()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProducts_WithDescendingSort_ShouldReturnSortedProducts() throws Exception {
        List<ProductResponse> products = Arrays.asList(productResponse);
        Page<ProductResponse> productPage = new PageImpl<>(products, PageRequest.of(0, 5), 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getById_ShouldReturnProduct() throws Exception {
        when(productReadService.getById(eq(productId))).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void listActive_ShouldReturnPagedProducts() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(productResponse), PageRequest.of(0, 10), 1);
        when(productQueryService.listActive(eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/products/active")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(productId.toString()));
    }

    @Test
    void listByCategory_ShouldReturnPagedProducts() throws Exception {
        UUID categoryId = UUID.randomUUID();
        Page<ProductResponse> page = new PageImpl<>(List.of(productResponse), PageRequest.of(0, 10), 1);
        when(productQueryService.listByCategory(eq(categoryId), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/products/by-category/{categoryId}", categoryId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(productId.toString()));
    }
}