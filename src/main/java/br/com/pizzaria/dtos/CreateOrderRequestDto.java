package br.com.pizzaria.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequestDto(

        @NotNull(message = "Table number is required")
        @Positive(message = "Table number must be positive")
        Integer table,

        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String name
) {

    public CreateOrderRequestDto {
        name = name == null ? null : name.trim();
    }
}
