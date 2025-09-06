package se.moln.productservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import se.moln.productservice.dto.ProductResponse;
import se.moln.productservice.mappning.ProductMapper;
import se.moln.productservice.model.Product;
import se.moln.productservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock private ProductRepository repo;
    @Mock private ProductMapper mapper;

    @InjectMocks private ProductQueryService service;

    private UUID id;
    private UUID categoryId;
    private Product entity;
    private ProductResponse dto;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        entity = new Product();
        entity.setId(id);
        entity.setName("Test");

        dto = new ProductResponse(
                id, "Test", "test", "Desc",
                BigDecimal.valueOf(10), "SEK", null, 0, true,
                java.util.Map.of(), java.util.List.of()
        );
    }

    @Test
    void getById_ReturnsMappedDto() {
        when(repo.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        ProductResponse out = service.getById(id);
        assertThat(out).isNotNull();
        assertThat(out.id()).isEqualTo(id);
        verify(repo).findById(id);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getById_NotFound_Throws404() {
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void listActive_MapsPage() {
        Page<Product> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(repo.findByActiveTrue(any())).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(dto);

        Page<ProductResponse> out = service.listActive(0, 10);
        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().get(0).id()).isEqualTo(id);
        verify(repo).findByActiveTrue(any());
        verify(mapper).toResponse(entity);
    }

    @Test
    void listByCategory_MapsPage() {
        Page<Product> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(repo.findByCategory_IdAndActiveTrue(eq(categoryId), any())).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(dto);

        Page<ProductResponse> out = service.listByCategory(categoryId, 0, 10);
        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().get(0).id()).isEqualTo(id);
        verify(repo).findByCategory_IdAndActiveTrue(eq(categoryId), any());
        verify(mapper).toResponse(entity);
    }
}