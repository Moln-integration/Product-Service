package se.moln.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.moln.productservice.dto.PageResponse;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.service.ProductImageAppService;
import se.moln.productservice.service.ProductQueryService;
import se.moln.productservice.service.ProductReadService;
import se.moln.productservice.service.ProductService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final ProductImageAppService imageService;
    private final ProductReadService readService;
    private final ProductQueryService queryService;

    public ProductController(ProductService service,
                             ProductImageAppService imageService,
                             ProductReadService readService,
                             ProductQueryService queryService) {
        this.service = service;
        this.imageService = imageService;
        this.readService = readService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest req) {
        System.out.println("kontroller");
        System.out.println("hello");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        System.out.println("Hämtar alla produkter med paginering från kontroller");

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> products = service.getAllProducts(pageable);
        PageResponse<ProductResponse> response = new PageResponse<>(products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProductsWithoutPagination() {
        List<ProductResponse> products = service.getAllProductsWithoutPagination();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<ProductResponse> products = service.searchProducts(name, categoryName, minPrice, maxPrice);
        System.out.println("hello");
        return ResponseEntity.ok(products);
    }

    // === Bilduppladdning (återställd) ===
    @Operation(
            summary = "Ladda upp produktbild",
            description = "Skicka som multipart/form-data med fältet 'file'. Bilden sparas lokalt och kopplas till produkten."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(
            path = "/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProductResponse> uploadImage(
            @Parameter(name = "id", description = "Produktens UUID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3")
            @PathVariable UUID id,
            @Parameter(description = "Bildfil (jpg/png/webp)")
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageService.uploadImage(id, file));
    }

    // === Hämta produkt per ID (flyttat in) ===
    @Operation(
            summary = "Hämta produkt per ID",
            description = "Returnerar en produkt baserat på dess UUID. Om produkten inte finns returneras 404."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produkt hittad",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Produkt ej hittad"),
            @ApiResponse(responseCode = "400", description = "Ogiltigt UUID-format")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @Parameter(name = "id", description = "Produktens UUID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3")
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(readService.getById(id));
    }

    // === Lista aktiva (unik path för att undvika krock med root) ===
    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponse>> listActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryService.listActive(page, size));
    }

    // === Lista per kategori (flyttat in) ===
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> listByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryService.listByCategory(categoryId, page, size));
    }
}