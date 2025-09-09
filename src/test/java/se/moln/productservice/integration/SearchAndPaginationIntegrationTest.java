package se.moln.productservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SearchAndPaginationIntegrationTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    private String macAirName;
    private String macProName;

    private TestRestTemplate auth() { return rest.withBasicAuth("admin", "demo"); }
    private String base(String p){ return "http://localhost:"+port+p; }

    private String searchUrl(Map<String, ?> params) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(base("/api/products/search"));
        params.forEach((k,v) -> { if (v != null) b.queryParam(k, v); });
        return b.toUriString();
    }

    @BeforeEach
    void seed() {
        String sfx = "-" + UUID.randomUUID();
        macAirName = "MacBook Air" + sfx;
        macProName = "MacBook Pro" + sfx;

        create(macAirName, new BigDecimal("14000"), "Laptops");
        create(macProName, new BigDecimal("25000"), "Laptops");
        create("iPhone 15" + sfx, new BigDecimal("12000"), "Phones");
    }

    private void create(String name, BigDecimal price, String category) {
        ProductRequest req = new ProductRequest(
                name, "d", price, "SEK",
                null, category, 5, Map.of(), List.of()
        );
        ResponseEntity<ProductResponse> resp =
                auth().postForEntity(base("/api/products"), req, ProductResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
    }

      @Test
    void pagination_and_sorting() {
        ResponseEntity<Map> page =
                auth().getForEntity(base("/api/products?page=0&size=2&sortBy=price&sortDir=asc"), Map.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<?,?> body = Objects.requireNonNull(page.getBody());
        List<?> content = (List<?>) body.get("content");
        Number total = (Number) body.get("totalElements");

        assertThat(content.size()).isEqualTo(2);
        assertThat(total.longValue()).isGreaterThanOrEqualTo(3);
    }
}
