package br.com.pizzaria.service;

import br.com.pizzaria.dtos.AddOrderItemRequestDto;
import br.com.pizzaria.dtos.OrderItemResponseDto;
import br.com.pizzaria.dtos.OrderRequestDto;
import br.com.pizzaria.dtos.OrderResponseDto;
import br.com.pizzaria.entity.Order;
import br.com.pizzaria.entity.OrderItem;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.exception.OrderNotDraftException;
import br.com.pizzaria.exception.OrderNotFoundException;
import br.com.pizzaria.exception.ProductInactiveException;
import br.com.pizzaria.exception.ProductNotFoundException;
import br.com.pizzaria.mapper.OrderItemMapper;
import br.com.pizzaria.mapper.OrderMapper;
import br.com.pizzaria.repository.OrderItemRepository;
import br.com.pizzaria.repository.OrderRepository;
import br.com.pizzaria.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;


    @Transactional
    public OrderResponseDto create(OrderRequestDto dto){
        Order order = orderMapper.toEntity(dto);
        Order saved = orderRepository.save(order);

        return orderMapper.toResponse(saved, List.of());
    }

    @Transactional
    public OrderItemResponseDto addItem(UUID orderId, AddOrderItemRequestDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));
        if (!order.isDraft()) {
            throw new OrderNotDraftException("Cannot add items to an order that is not a draft");
        }

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
        if (product.isDisabled()) {
            throw new ProductInactiveException("Cannot add an inactive product to an order");
        }

        OrderItem item = orderItemMapper.toEntity(dto);
        item.setOrder(order);
        item.setProduct(product);

        OrderItem saved = orderItemRepository.save(item);
        return orderItemMapper.toResponse(saved);
    }
}
