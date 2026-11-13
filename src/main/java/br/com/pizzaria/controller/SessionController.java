package br.com.pizzaria.controller;


import br.com.pizzaria.dtos.SessionRequestDto;
import br.com.pizzaria.dtos.SessionResponseDto;
import br.com.pizzaria.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponseDto> login(@Valid @RequestBody SessionRequestDto dto){
        return ResponseEntity.ok(sessionService.login(dto));
    }
}
