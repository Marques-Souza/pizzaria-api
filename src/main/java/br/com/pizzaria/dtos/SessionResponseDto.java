package br.com.pizzaria.dtos;

public record SessionResponseDto(

        String accessToken,
        UserResponseDto user
) {
}
