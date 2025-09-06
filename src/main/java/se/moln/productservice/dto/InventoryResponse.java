package se.moln.productservice.dto;

import se.moln.productservice.model.StockStatus;

import java.util.UUID;

public record InventoryResponse(
        UUID productId,
        int quantity,
        StockStatus status
) {
}
