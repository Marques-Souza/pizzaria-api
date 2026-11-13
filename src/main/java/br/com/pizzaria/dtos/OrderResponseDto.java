package br.com.pizzaria.dtos;

import java.util.List;
import java.util.UUID;

public record OrderResponseDto(

        UUID id,
        Integer table,
        String name,
        boolean status,
        boolean draft,
        List<OrderItemResponseDto> items
) {
}
