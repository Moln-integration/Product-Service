package se.moln.productservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import se.moln.productservice.dto.ProductRequest;
import se.moln.productservice.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApiIntegrationTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    private TestRestTemplate auth() { return rest.withBasicAuth("admin", "demo"); }
    private String url(String p){ return "http://localhost:"+port+p; }

    private ProductRequest req(String name, BigDecimal price, String category) {
        return new ProductRequest(
                name, "Desc", price, "SEK",
                null, category, 10, Map.of("k","v"), List.of()
        );
    }

    @Test
    void create_get_update_delete() {
        ResponseEntity<ProductResponse> created =
                auth().postForEntity(url("/api/products"), req("IT Phone A", new BigDecimal("1234.56"), "Phones"), ProductResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID id = created.getBody().id();

        ProductResponse got = auth().getForObject(url("/api/products/"+id), ProductResponse.class);
        assertThat(got.id()).isEqualTo(id);

        ProductRequest update = req("IT Phone A+",
                new BigDecimal("1999.00"), "Phones");
        ResponseEntity<ProductResponse> updated = auth().exchange(
                url("/api/products/"+id),
                HttpMethod.PUT,
                new HttpEntity<>(update),
                ProductResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("IT Phone A+");

        ResponseEntity<Void> del = auth().exchange(url("/api/products/"+id), HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> afterDel = auth().getForEntity(url("/api/products/"+id), String.class);
        assertThat(afterDel.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void upload_image() {
        ProductResponse p = auth().postForObject(url("/api/products"), req("IT Cam", new BigDecimal("500.00"), "Cameras"), ProductResponse.class);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource file = new ByteArrayResource("bytes".getBytes()) { @Override public String getFilename() { return "test.jpg"; } };
        body.add("file", file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<ProductResponse> resp = auth().postForEntity(
                url("/api/products/"+p.id()+"/images"),
                new HttpEntity<>(body, headers),
                ProductResponse.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().images()).isNotEmpty();
    }
}