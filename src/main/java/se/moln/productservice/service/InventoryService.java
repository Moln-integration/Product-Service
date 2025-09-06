package se.moln.productservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.moln.productservice.dto.InventoryResponse;
import se.moln.productservice.model.Inventory;
import se.moln.productservice.model.Product;
import se.moln.productservice.model.StockStatus;
import se.moln.productservice.repository.InventoryRepository;
import se.moln.productservice.repository.ProductRepository;

import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final ProductRepository productRepo;

    public InventoryService(InventoryRepository inventoryRepo, ProductRepository productRepo) {
        this.inventoryRepo = inventoryRepo;
        this.productRepo = productRepo;
    }

    @Transactional(readOnly = true)
    public InventoryResponse get(UUID productId) {
        Inventory inv = getOrCreate(productId);
        return toResponse(inv);
    }

    @Transactional
    public InventoryResponse purchase(UUID productId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("quantity must be > 0");
        Inventory inv = getOrCreate(productId);
        if (inv.getQuantity() < qty) {
            throw new IllegalArgumentException("not enough stock");
        }
        inv.setQuantity(inv.getQuantity() - qty);
        inv.setStatus(inv.getQuantity() > 0 ? StockStatus.IN_STOCK : StockStatus.OUT_OF_STOCK);
        inventoryRepo.save(inv);
        return toResponse(inv);
    }

    @Transactional
    public InventoryResponse refund(UUID productId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("quantity must be > 0");
        Inventory inv = getOrCreate(productId);
        inv.setQuantity(inv.getQuantity() + qty);
        inv.setStatus(StockStatus.IN_STOCK);
        inventoryRepo.save(inv);
        return toResponse(inv);
    }

    private Inventory getOrCreate(UUID productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        return inventoryRepo.findByProductId(productId).orElseGet(() -> {
            Inventory n = new Inventory();
            n.setProduct(product);
            n.setQuantity(0);
            n.setStatus(StockStatus.OUT_OF_STOCK);
            return inventoryRepo.save(n);
        });
    }

    private InventoryResponse toResponse(Inventory inv) {
        return new InventoryResponse(inv.getProduct().getId(), inv.getQuantity(), inv.getStatus());
    }
}