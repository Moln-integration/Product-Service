package se.moln.productservice.mappning;

import org.springframework.stereotype.Component;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.model.Category;
import se.moln.productservice.model.Product;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest req, Category cat) {
        Product p = new Product();
        p.setName(req.name());
        p.setSlug(slugify(req.name()));
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setCurrency(req.currency());
        p.setCategory(cat);
        p.setStockQuantity(req.stockQuantity() != null ? req.stockQuantity() : 0);
        if (req.attributes() != null) {
            p.setAttributes(new LinkedHashMap<>(req.attributes()));
        }
        if (req.imageUrl() != null) {
            p.setImageUrl(req.imageUrl());
        }
        return p;
    }

    public ProductResponse toResponse(Product p) {
        Map<String, String> attrs =
                p.getAttributes() == null ? Map.of() : new LinkedHashMap<>(p.getAttributes());

        String imageUrl = p.getImageUrl();
        return new ProductResponse(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getPrice(), p.getCurrency(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getStockQuantity(), p.isActive(),
                attrs, imageUrl
        );
    }

    private String slugify(String s) {
        return s.toLowerCase().trim()
                .replaceAll("[^a-z0-9]+","-")
                .replaceAll("(^-|-$)","");
    }
}