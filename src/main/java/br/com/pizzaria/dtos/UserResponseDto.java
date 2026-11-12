package br.com.pizzaria.dtos;

import br.com.pizzaria.enums.Role;

import java.util.UUID;

public record UserResponseDto(

        UUID id,
        String name,
        String email,
        Role role
) {
}
