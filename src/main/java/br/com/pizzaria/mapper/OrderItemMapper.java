package br.com.pizzaria.mapper;


import br.com.pizzaria.dtos.AddOrderItemRequestDto;
import br.com.pizzaria.dtos.OrderItemResponseDto;
import br.com.pizzaria.dtos.ProductInOrderResponseDto;
import br.com.pizzaria.entity.OrderItem;
import br.com.pizzaria.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(AddOrderItemRequestDto dto);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "productId", source = "product.id")
    OrderItemResponseDto toResponse(OrderItem item);

    ProductInOrderResponseDto toResponse(Product product);
}
