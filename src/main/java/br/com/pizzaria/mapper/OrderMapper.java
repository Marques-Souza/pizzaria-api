package br.com.pizzaria.mapper;


import br.com.pizzaria.dtos.CreateOrderRequestDto;
import br.com.pizzaria.dtos.OrderItemResponseDto;
import br.com.pizzaria.dtos.OrderResponseDto;
import br.com.pizzaria.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tableNumber", source = "table")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(CreateOrderRequestDto dto);

    @Mapping(target = "table", source = "order.tableNumber")
    @Mapping(target = "items", source = "items")
    OrderResponseDto toResponse(Order order, List<OrderItemResponseDto> items);
}
