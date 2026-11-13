package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.SessionRequestDto;
import br.com.pizzaria.dtos.SessionResponseDto;
import br.com.pizzaria.dtos.UserResponseDto;
import br.com.pizzaria.enums.Role;
import br.com.pizzaria.exception.GlobalExceptionHandler;
import br.com.pizzaria.service.SessionService;
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

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private SessionService sessionService;
    @InjectMocks
    private SessionController sessionController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sessionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(
                        Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void loginReturnsAccessTokenAndUser() throws Exception {
        UUID userId = UUID.randomUUID();
        SessionRequestDto request =
                new SessionRequestDto("maria@example.com", "senha123");
        SessionResponseDto response = new SessionResponseDto(
                "jwt-token",
                new UserResponseDto(userId, "Maria", "maria@example.com", Role.STAFF));
        when(sessionService.login(request)).thenReturn(response);

        mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria@example.com",
                                  "password": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()));
    }

    @Test
    void loginReturns400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }
}
