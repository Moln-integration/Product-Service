package se.moln.productservice.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "inventory",
        indexes = @Index(name = "idx_inventory_product", columnList = "product_id", unique = true)
)
public class Inventory {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private int quantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockStatus status = StockStatus.OUT_OF_STOCK;

    @Version
    private Long version;

    // getters/setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product; }

    public int getQuantity() {
        return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity; }

    public StockStatus getStatus() {
        return status; }

    public void setStatus(StockStatus status) {
        this.status = status; }

    public Long getVersion() {
        return version; }

    public void setVersion(Long version) {
        this.version = version; }
}
