package se.moln.productservice.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name= "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 200)
    @Column(unique = true, length = 200)
    private String slug;

    @Lob
    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column private BigDecimal price;

    @NotNull
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Min(0)
    @Column(nullable = false)
    private int stockQuantity = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @OrderColumn(name = "position")
    private List<ProductImage> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_attributes",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @MapKeyColumn(name = "attr_key", length = 100)
    @Column(name = "attr_value", columnDefinition = "nvarchar(max)")
    private Map<String, String> attributes = new LinkedHashMap<>();

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

//    public void reserveStock(int quantity) {
//        if (quantity <= 0) throw new IllegalArgumentException("quantity måste vara > 0");
//        if (this.stockQuantity < quantity) {
//            throw new IllegalStateException("Otillräckligt lager");
//        }
//        this.stockQuantity -= quantity;
//    }

    public void reserveStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity måste vara > 0");
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("Otillräckligt lager");
        }
        this.stockQuantity -= quantity;
        //TODO om stock blir 0 då isactiv blir false
    }
    public void releaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity måste vara > 0");
        this.stockQuantity += quantity;
    }

    public void addImage(String url, String fileName, String contentType, Long sizeBytes) {
        this.images.add(new ProductImage(url, fileName, contentType, sizeBytes));
    }

    public void putAttribute(String key, String value) {
        this.attributes.put(Objects.requireNonNull(key), value);
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}