package br.com.pizzaria.dtos;

import jakarta.validation.constraints.Size;

public record CategoryUpdateDto(


        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        String name,

        @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
        String description
) {
}
