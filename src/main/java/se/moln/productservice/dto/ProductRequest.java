package se.moln.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(name = "ProductRequest", description = "skapa en produkt")
public record ProductRequest(
        @Schema(example = "iPhone 16 Pro", description = "Produktens namn")
        @NotBlank String name,

        @Schema(example = "Apples flaggskepp med USB-C och A18", description = "Beskrivning (lång text tillåten)")
        String description,

        @Schema(example = "12999.00", description = "Pris exkl. eller inkl. moms beroende på din domän")
        @NotNull @Positive BigDecimal price,

        @Schema(example = "SEK", description = "Valutakod enligt ISO 4217")
        @NotBlank String currency,


        @Schema(example = "null", nullable = true,
                description = "Valfritt. Kategori-ID om du vill länka till befintlig kategori")
        UUID categoryId,


        @Schema(example = "Mobiler", nullable = true,
                description = "Valfritt. Namn på kategori om du inte har ID. Skapas vid behov i dev/demo.")
        String categoryName,


        @Schema(example = "10", description = "Initialt lagersaldo")
        @Min(0) Integer stockQuantity,

        @Schema(
                description = "Nyckel-värde-attribut (filtreras/normaliseras i servern)",
                example = """
            {"color":"space gray","ram":"32GB","storage":"256GB"}
            """
        )
        Map<String,String> attributes,

        @Schema(
                description = "Lista med publika bild-URL:er (eller blob-URL:er)",
                example = "https://example.com/images/iphone16-front.jpg"
        )
        String imageUrl
) {}