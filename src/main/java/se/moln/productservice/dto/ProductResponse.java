package se.moln.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(name = "ProductResponse", description = "Produkt som returneras från API:t")
public record ProductResponse(
        @Schema(example = "b7f8f3f9-3e8c-4612-9d8a-1d4c9f77a001") UUID id,
        @Schema(example = "iPhone 16 Pro") String name,
        @Schema(example = "iphone-16-pro") String slug,
        @Schema(example = "Apples flaggskepp med USB-C och A18") String description,
        @Schema(example = "12999.00") BigDecimal price,
        @Schema(example = "SEK") String currency,
        @Schema(example = "Mobiler", nullable = true) String categoryName,
        @Schema(example = "10") int stockQuantity,
        @Schema(example = "true") boolean active,
        @Schema(example = """
            {"color":"space gray","ram":"32GB","storage":"256GB"}
        """) Map<String,String> attributes,
        @Schema(example = "https://example.com/images/iphone16-front.jpg")
        String imageUrl
) {}






//package se.moln.productservice.dto;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public record ProductResponse(
//        UUID id, String name, String slug, String description,
//        BigDecimal price, String currency, String categoryName,
//        int stockQuantity, boolean active,
//        Map<String,String> attributes, List<String> images
//) {}