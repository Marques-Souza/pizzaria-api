package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.dtos.CategoryUpdateDto;
import br.com.pizzaria.exception.GlobalExceptionHandler;
import br.com.pizzaria.service.CategoryService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private CategoryController categoryController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(
                        Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void createReturns201() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponseDto response = new CategoryResponseDto(
                id, "Pizzas", "Pizzas salgadas", true);
        when(categoryService.create(new CategoryRequestDto("Pizzas", "Pizzas salgadas")))
                .thenReturn(response);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pizzas","description":"Pizzas salgadas"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createReturns400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","description":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void findAllReturnsCategories() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
                new CategoryResponseDto(UUID.randomUUID(), "Pizzas", "Salgadas", true)));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pizzas"));
    }

    @Test
    void findByIdReturnsCategory() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.findById(id))
                .thenReturn(new CategoryResponseDto(id, "Pizzas", "Salgadas", true));

        mockMvc.perform(get("/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void updateReturnsUpdatedCategory() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponseDto response =
                new CategoryResponseDto(id, "Bebidas", "Bebidas frias", true);
        when(categoryService.update(id, new CategoryUpdateDto("Bebidas", "Bebidas frias")))
                .thenReturn(response);

        mockMvc.perform(patch("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bebidas","description":"Bebidas frias"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bebidas"));
    }

    @Test
    void deleteReturns204WithoutBody() throws Exception {
        doNothing().when(categoryService).delete(UUID.fromString(
                "d628d2a0-479b-498c-84d4-8dd1be18de2e"));

        mockMvc.perform(delete("/categories/{id}",
                        UUID.fromString("d628d2a0-479b-498c-84d4-8dd1be18de2e")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).delete(UUID.fromString(
                "d628d2a0-479b-498c-84d4-8dd1be18de2e"));
    }
}
