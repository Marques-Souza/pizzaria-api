package br.com.pizzaria.service;

import br.com.pizzaria.dtos.*;
import br.com.pizzaria.entity.Order;
import br.com.pizzaria.entity.OrderItem;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.exception.*;
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

    @Transactional(readOnly = true)
    public OrderDetailResponseDto findOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));
        List<OrderDetailItemResponseDto> items =
                orderItemRepository.findAllByOrder_Id(id)
                        .stream()
                        .map(orderItemMapper::toDetailResponse)
                        .toList();
        return orderMapper.toDetailResponse(order, items);
    }

    @Transactional
    public void removeItem(UUID orderId, UUID itemId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));
        if (!order.isDraft()){
            throw new OrderNotDraftException("Cannot remove items from an order that is not a draft");
        }

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderItemNotFoundException("Order item not found."));
        if (!item.getOrder().getId().equals(orderId)){
            throw new OrderItemNotFoundException("Order item not found in this order.");
        }
        orderItemRepository.delete(item);
    }

    @Transactional
    public OrderDetailResponseDto sendOrder(UUID orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));
        if (!order.isDraft()){
            throw new OrderNotDraftException("Order has already been sent.");
        }

        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_Id(orderId);
        if (orderItems.isEmpty()) {
            throw new OrderEmptyException("Cannot send an order without items.");
        }

        order.setDraft(false);
        Order saved = orderRepository.save(order);

        List<OrderDetailItemResponseDto> items = orderItems.stream()
                .map(orderItemMapper::toDetailResponse)
                .toList();

        return orderMapper.toDetailResponse(saved, items);
    }

    @Transactional
    public OrderDetailResponseDto finishOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));
        if (order.isDraft()) {
            throw new OrderNotSentException("Cannot finish an order that has not been sent.");
        }
        if (order.isStatus()) {
            throw new OrderAlreadyFinishedException("Order has already been finished.");
        }

        order.setStatus(true);
        Order saved = orderRepository.save(order);

        List<OrderDetailItemResponseDto> items =
                orderItemRepository.findAllByOrder_Id(orderId)
                        .stream()
                        .map(orderItemMapper::toDetailResponse)
                        .toList();

        return orderMapper.toDetailResponse(saved, items);
    }
}
