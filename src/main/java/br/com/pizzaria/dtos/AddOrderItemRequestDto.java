package br.com.pizzaria.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddOrderItemRequestDto(

        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Integer amount
) {
}
