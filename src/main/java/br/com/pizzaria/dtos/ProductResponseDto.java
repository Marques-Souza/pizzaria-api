package br.com.pizzaria.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDto(


        UUID id,
        String name,
        String description,
        BigDecimal price,
        UUID categoryId,
        String categoryName,
        String banner,
        boolean disabled
) {
}
