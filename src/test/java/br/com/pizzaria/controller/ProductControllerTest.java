package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.dtos.ProductUpdateDto;
import br.com.pizzaria.exception.GlobalExceptionHandler;
import br.com.pizzaria.service.ProductService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;
    @InjectMocks
    private ProductController productController;
    private MockMvc mockMvc;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(
                        Validation.buildDefaultValidatorFactory().getValidator()))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createAcceptsMultipartFieldsAndImage() throws Exception {
        ProductResponseDto response = productResponse();
        when(productService.create(
                eq(new ProductRequestDto(
                        "Coca-Cola", "Refrigerante", new BigDecimal("10.50"), categoryId)),
                any(MultipartFile.class)))
                .thenReturn(response);
        MockMultipartFile file =
                new MockMultipartFile("file", "product.webp", "image/webp", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/products")
                        .file(file)
                        .param("name", "Coca-Cola")
                        .param("description", "Refrigerante")
                        .param("price", "10.50")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.price").value(10.50));
    }

    @Test
    void findByIdReturnsProduct() throws Exception {
        when(productService.findById(productId)).thenReturn(productResponse());

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Coca-Cola"));
    }

    @Test
    void findAllReturnsPagedProductsAndPassesCategoryFilter() throws Exception {
        PageRequest pageable = PageRequest.of(
                0, 10, Sort.by(Sort.Direction.DESC, "name"));
        when(productService.findAll(pageable, categoryId))
                .thenReturn(new PageImpl<>(List.of(productResponse()), pageable, 1));

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Coca-Cola"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).findAll(pageable, categoryId);
    }

    @Test
    void updateAcceptsMultipartFields() throws Exception {
        ProductUpdateDto update = new ProductUpdateDto(
                "Coca-Cola Zero", null, null, null);
        ProductResponseDto response = new ProductResponseDto(
                productId, "Coca-Cola Zero", "Refrigerante", new BigDecimal("10.50"),
                categoryId, "Bebidas", "https://images.test/product.webp", false);
        when(productService.update(productId, update, null)).thenReturn(response);
        MockMultipartHttpServletRequestBuilder request =
                multipart("/products/{id}", productId).with(
                        servletRequest -> {
                            servletRequest.setMethod("PATCH");
                            return servletRequest;
                        });

        mockMvc.perform(request.param("name", "Coca-Cola Zero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coca-Cola Zero"));
    }

    @Test
    void deleteReturns204WithoutBody() throws Exception {
        doNothing().when(productService).delete(productId);

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).delete(productId);
    }

    @Test
    void reactivateReturnsProduct() throws Exception {
        when(productService.reactivate(productId)).thenReturn(productResponse());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/products/{id}/reactivate", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()));
    }

    private ProductResponseDto productResponse() {
        return new ProductResponseDto(
                productId,
                "Coca-Cola",
                "Refrigerante",
                new BigDecimal("10.50"),
                categoryId,
                "Bebidas",
                "https://images.test/product.webp",
                false
        );
    }
}
