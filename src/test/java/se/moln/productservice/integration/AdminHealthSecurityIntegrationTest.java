package se.moln.productservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminHealthSecurityIntegrationTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    private String url(String p){ return "http://localhost:"+port+p; }

    @Test
    void unauthorized_without_basic_auth() {
        ResponseEntity<String> resp = rest.getForEntity(url("/api/admin/health-details"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void ok_with_admin_role() {
        ResponseEntity<String> resp = rest.withBasicAuth("admin","demo")
                .getForEntity(url("/api/admin/health-details"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("\"status\"");
    }
}
