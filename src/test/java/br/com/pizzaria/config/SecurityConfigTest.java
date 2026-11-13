package br.com.pizzaria.config;

import br.com.pizzaria.controller.CategoryController;
import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    private static final String TOKEN = "security" + "-test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanReadCategoriesButCannotCreateUpdateOrDelete() throws Exception {
        mockJwt("STAFF");

        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pizzas\",\"description\":\"Salgadas\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/categories/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pizzas\",\"description\":\"Salgadas\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/categories/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateCategories() throws Exception {
        UUID id = UUID.randomUUID();
        mockJwt("ADMIN");
        when(categoryService.create(any(CategoryRequestDto.class)))
                .thenReturn(new CategoryResponseDto(id, "Pizzas", "Salgadas", true));

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pizzas\",\"description\":\"Salgadas\"}"))
                .andExpect(status().isCreated());
    }

    private void mockJwt(String role) {
        when(jwtDecoder.decode(TOKEN))
                .thenReturn(Jwt.withTokenValue(TOKEN)
                        .header("alg", "none")
                        .claim("role", role)
                        .build());
    }
}
