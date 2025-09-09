package se.moln.productservice.integration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import se.moln.productservice.dto.AdjustStockRequest;
import se.moln.productservice.dto.InventoryResponse;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryApiIntegrationTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    private TestRestTemplate auth() { return rest.withBasicAuth("admin", "demo"); }
    private String url(String p){ return "http://localhost:"+port+p; }

    private UUID newProduct() {
        ProductRequest req = new ProductRequest(
                "IT Stock Item "+System.nanoTime(), "stock", new BigDecimal("10.00"), "SEK",
                null, "Stock", 0, Map.of(), List.of()
        );
        return auth().postForObject(url("/api/products"), req, ProductResponse.class).id();
    }

    @Test
    void refund_and_purchase_flow() {
        UUID id = newProduct();

        InventoryResponse start = auth().getForObject(url("/api/inventory/"+id), InventoryResponse.class);
        assertThat(start.quantity()).isZero();

        InventoryResponse afterRefund = auth().postForObject(url("/api/inventory/"+id+"/return"),
                new AdjustStockRequest(5), InventoryResponse.class);
        assertThat(afterRefund.quantity()).isEqualTo(5);

        InventoryResponse afterBuy = auth().postForObject(url("/api/inventory/"+id+"/purchase"),
                new AdjustStockRequest(3), InventoryResponse.class);
        assertThat(afterBuy.quantity()).isEqualTo(2);
    }

    @Test
    void purchase_not_enough_returns_400() {
        UUID id = newProduct();
        auth().postForObject(url("/api/inventory/"+id+"/return"), new AdjustStockRequest(1), InventoryResponse.class);

        ResponseEntity<String> resp = auth().postForEntity(url("/api/inventory/"+id+"/purchase"),
                new AdjustStockRequest(5), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}