package br.com.pizzaria.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponseDto(

        UUID id,
        Integer table,
        String name,
        boolean status,
        boolean draft,
        LocalDateTime createdAt,
        List<OrderDetailItemResponseDto> items

) {
}
