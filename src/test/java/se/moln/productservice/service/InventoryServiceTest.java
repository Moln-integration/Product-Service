package se.moln.productservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.moln.productservice.dto.InventoryResponse;
import se.moln.productservice.model.Inventory;
import se.moln.productservice.model.Product;
import se.moln.productservice.model.StockStatus;
import se.moln.productservice.repository.InventoryRepository;
import se.moln.productservice.repository.ProductRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private InventoryService service;

    private UUID productId;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(5);
        inventory.setStatus(StockStatus.IN_STOCK);
    }

    @Test
    void get_WhenInventoryExists_ReturnsResponse() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        InventoryResponse resp = service.get(productId);

        assertThat(resp).isNotNull();
        assertThat(resp.productId()).isEqualTo(productId);
        assertThat(resp.quantity()).isEqualTo(5);
        assertThat(resp.status()).isEqualTo(StockStatus.IN_STOCK);
    }

    @Test
    void get_WhenInventoryMissing_CreatesAndReturnsDefault() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse resp = service.get(productId);

        assertThat(resp.productId()).isEqualTo(productId);
        assertThat(resp.quantity()).isZero();
        assertThat(resp.status()).isEqualTo(StockStatus.OUT_OF_STOCK);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void get_WhenProductMissing_Throws() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void purchase_WithEnoughStock_DecreasesQuantityAndUpdatesStatus() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        InventoryResponse resp = service.purchase(productId, 3);

        assertThat(resp.quantity()).isEqualTo(2);
        assertThat(resp.status()).isEqualTo(StockStatus.IN_STOCK);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void purchase_ExactStock_ReachesZeroSetsOutOfStock() {
        inventory.setQuantity(2);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        InventoryResponse resp = service.purchase(productId, 2);

        assertThat(resp.quantity()).isZero();
        assertThat(resp.status()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void purchase_NotEnoughStock_Throws() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> service.purchase(productId, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not enough stock");
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void purchase_InvalidQuantity_Throws() {
        assertThatThrownBy(() -> service.purchase(productId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity must be > 0");
    }

    @Test
    void refund_IncreasesQuantityAndSetsInStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        InventoryResponse resp = service.refund(productId, 4);

        assertThat(resp.quantity()).isEqualTo(9);
        assertThat(resp.status()).isEqualTo(StockStatus.IN_STOCK);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void refund_InvalidQuantity_Throws() {
        assertThatThrownBy(() -> service.refund(productId, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity must be > 0");
    }
}
