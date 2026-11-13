package br.com.pizzaria.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductInOrderResponseDto(

        UUID id,
        String name,
        BigDecimal price,
        String description,
        String banner
) {
}
