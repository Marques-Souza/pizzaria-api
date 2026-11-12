package br.com.pizzaria.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record UserUpdateDto(

        @Size(min = 1, max = 255, message = "Name must have 1 to 255 characters")
        String name,

        @Email(message = "Email must be a valid email address")
        @Size(min = 1, max = 255, message = "Email must have 1 to 255 characters")
        String email
) {

    public UserUpdateDto {
        name = name == null ? null : name.trim();
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
