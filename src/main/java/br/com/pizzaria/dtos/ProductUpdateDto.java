package br.com.pizzaria.dtos;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateDto(

        @Size(min = 1, max = 100, message = "Product name must have 1 to 100 characters")
        String name,

        @Size(min = 1, max = 255, message = "Product description must have 1 to 255 characters")
        String description,

        @Positive(message = "Price must be a positive value")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,

        UUID categoryId
) {

    public ProductUpdateDto {
        name = name == null ? null : name.trim();
        description = description == null ? null : description.trim();
    }
}
