package br.com.pizzaria.dtos;

import java.util.UUID;

public record OrderItemResponseDto(
        UUID id,
        Integer amount,
        UUID orderId,
        UUID productId,
        ProductInOrderResponseDto product
) {
}
