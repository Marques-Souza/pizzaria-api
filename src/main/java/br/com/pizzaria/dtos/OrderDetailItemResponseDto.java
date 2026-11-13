package br.com.pizzaria.dtos;

import java.util.UUID;

public record OrderDetailItemResponseDto(

        UUID id,
        Integer amount,
        ProductInOrderResponseDto product
) {
}
