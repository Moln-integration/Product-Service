package se.moln.productservice.dto;

import jakarta.validation.constraints.Min;

public record AdjustStockRequest(
        @Min(1) int quantity
) {}
