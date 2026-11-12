package br.com.pizzaria.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record SessionRequestDto(


        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters")
        String password

) {

    public SessionRequestDto {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
