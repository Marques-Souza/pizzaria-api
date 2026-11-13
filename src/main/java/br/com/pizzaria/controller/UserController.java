package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.UserRequestDto;
import br.com.pizzaria.dtos.UserResponseDto;
import br.com.pizzaria.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserRequestDto dto){
        UserResponseDto response = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> findById(@AuthenticationPrincipal Jwt jwt){
       UUID id = UUID.fromString(jwt.getSubject());
       return ResponseEntity.ok(userService.findById(id));
    }
}
