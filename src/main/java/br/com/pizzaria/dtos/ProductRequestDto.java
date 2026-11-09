package br.com.pizzaria.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequestDto(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Product description must not exceed 255 characters")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be a positive value")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Category is required")
        UUID categoryId
) {

    public ProductRequestDto {
        name = name == null ? null : name.trim();
        description = description == null ? null : description.trim();
    }
}
