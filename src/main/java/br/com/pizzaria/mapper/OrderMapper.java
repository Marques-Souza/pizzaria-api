package br.com.pizzaria.mapper;


import br.com.pizzaria.dtos.OrderRequestDto;
import br.com.pizzaria.dtos.OrderDetailItemResponseDto;
import br.com.pizzaria.dtos.OrderDetailResponseDto;
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
    Order toEntity(OrderRequestDto dto);

    @Mapping(target = "table", source = "order.tableNumber")
    @Mapping(target = "items", source = "items")
    OrderResponseDto toResponse(Order order, List<OrderItemResponseDto> items);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "table", source = "order.tableNumber")
    @Mapping(target = "name", source = "order.name")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "draft", source = "order.draft")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "items", source = "items")
    OrderDetailResponseDto toDetailResponse(
            Order order,
            List<OrderDetailItemResponseDto> items
    );
}
