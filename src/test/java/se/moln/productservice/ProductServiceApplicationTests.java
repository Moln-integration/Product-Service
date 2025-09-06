package se.moln.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;
import se.moln.productservice.repository.CategoryRepository;
import se.moln.productservice.repository.InventoryRepository;
import se.moln.productservice.repository.ProductRepository;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.web-application-type=none",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
class ProductServiceApplicationTests {

    @TestConfiguration
    static class MockConfig {
        @Bean
        InventoryRepository inventoryRepository() {
            return Mockito.mock(InventoryRepository.class);
        }

        @Bean
        ProductRepository productRepository() {
            return Mockito.mock(ProductRepository.class);
        }

        @Bean
        CategoryRepository categoryRepository() {
            return Mockito.mock(CategoryRepository.class);
        }
    }

    @Test
    void contextLoads() {
    }

}
