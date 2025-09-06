package se.moln.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.moln.productservice.model.Inventory;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductId(UUID productId);
}