package br.com.pizzaria.controller;


import br.com.pizzaria.dtos.SessionRequestDto;
import br.com.pizzaria.dtos.SessionResponseDto;
import br.com.pizzaria.service.SessionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
@Tag(name = "Autenticacao", description = "Autenticacao e emissao de token JWT")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Autenticar usuario", description = "Valida email e senha e retorna um token JWT.")
    @SecurityRequirements
    public ResponseEntity<SessionResponseDto> login(@Valid @RequestBody SessionRequestDto dto){
        return ResponseEntity.ok(sessionService.login(dto));
    }
}
