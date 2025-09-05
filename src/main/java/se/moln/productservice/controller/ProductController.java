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

    public ProductController(ProductService service,
                             ProductImageAppService imageService) {
        this.service = service;
        this.imageService = imageService;
    }



    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProductResponse> create(
            @RequestPart("product") ProductRequest product,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {


        ProductResponse newProduct = service.create(product, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir){
        System.out.println("Hämtar alla produkter med paginering från kontroller");

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> products = service.getAllProducts(pageable);

        PageResponse<ProductResponse> response = new PageResponse<>(products);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProductsWithoutPagination(){
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
            @Parameter(
                    name = "id",
                    description = "Produktens UUID",
                    example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3"
            )
            @PathVariable UUID id,

            @Parameter(description = "Bildfil (jpg/png/webp)")
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageService.uploadImage(id, file));
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest req){

        System.out.println(req.categoryName());

        ProductResponse updateProduct = service.update(id, req);
        return ResponseEntity.ok(updateProduct);
    }
}