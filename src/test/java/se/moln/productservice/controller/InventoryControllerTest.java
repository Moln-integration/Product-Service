package se.moln.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.moln.productservice.dto.AdjustStockRequest;
import se.moln.productservice.dto.InventoryResponse;
import se.moln.productservice.model.StockStatus;
import se.moln.productservice.service.InventoryService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    private UUID productId;
    private InventoryResponse response;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        response = new InventoryResponse(productId, 5, StockStatus.IN_STOCK);
    }

    @Test
    void get_ShouldReturnInventory() throws Exception {
        when(inventoryService.get(eq(productId))).thenReturn(response);

        mockMvc.perform(get("/api/inventory/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    void purchase_ShouldReturnUpdatedInventory() throws Exception {
        when(inventoryService.purchase(eq(productId), eq(3))).thenReturn(new InventoryResponse(productId, 2, StockStatus.IN_STOCK));

        AdjustStockRequest req = new AdjustStockRequest(3);
        mockMvc.perform(post("/api/inventory/{productId}/purchase", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void return_ShouldReturnUpdatedInventory() throws Exception {
        when(inventoryService.refund(eq(productId), eq(4))).thenReturn(new InventoryResponse(productId, 9, StockStatus.IN_STOCK));

        AdjustStockRequest req = new AdjustStockRequest(4);
        mockMvc.perform(post("/api/inventory/{productId}/return", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(9))
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    void purchase_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // @Min(1) on AdjustStockRequest.quantity should trigger validation 400
        AdjustStockRequest req = new AdjustStockRequest(0);
        mockMvc.perform(post("/api/inventory/{productId}/purchase", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}