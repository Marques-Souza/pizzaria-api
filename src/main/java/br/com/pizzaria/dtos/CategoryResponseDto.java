package br.com.pizzaria.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponseDto(

        UUID id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt
) {
}
