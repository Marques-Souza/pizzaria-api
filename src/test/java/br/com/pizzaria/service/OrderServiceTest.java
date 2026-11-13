package br.com.pizzaria.service;

import br.com.pizzaria.dtos.AddOrderItemRequestDto;
import br.com.pizzaria.dtos.OrderDetailItemResponseDto;
import br.com.pizzaria.dtos.OrderDetailResponseDto;
import br.com.pizzaria.dtos.OrderItemResponseDto;
import br.com.pizzaria.dtos.OrderRequestDto;
import br.com.pizzaria.dtos.OrderResponseDto;
import br.com.pizzaria.entity.Order;
import br.com.pizzaria.entity.OrderItem;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.exception.OrderAlreadyFinishedException;
import br.com.pizzaria.exception.OrderEmptyException;
import br.com.pizzaria.exception.OrderNotDraftException;
import br.com.pizzaria.exception.OrderNotFoundException;
import br.com.pizzaria.exception.OrderNotSentException;
import br.com.pizzaria.exception.OrderItemNotFoundException;
import br.com.pizzaria.exception.ProductInactiveException;
import br.com.pizzaria.exception.ProductNotFoundException;
import br.com.pizzaria.mapper.OrderItemMapper;
import br.com.pizzaria.mapper.OrderMapper;
import br.com.pizzaria.repository.OrderItemRepository;
import br.com.pizzaria.repository.OrderRepository;
import br.com.pizzaria.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID orderId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    @Test
    void createSavesOrderAndReturnsEmptyItems() {
        OrderRequestDto request = new OrderRequestDto(4, "Maria");
        Order order = order(orderId, true, false);
        OrderResponseDto response = new OrderResponseDto(
                orderId, 4, "Maria", false, true, List.of());
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order, List.of())).thenReturn(response);

        assertSame(response, orderService.create(request));

        verify(orderRepository).save(order);
    }

    @Test
    void addItemAssociatesExistingDraftAndActiveProduct() {
        Order order = order(orderId, true, false);
        Product product = new Product();
        product.setId(productId);
        AddOrderItemRequestDto request = new AddOrderItemRequestDto(productId, 2);
        OrderItem item = new OrderItem();
        item.setAmount(2);
        OrderItemResponseDto response = new OrderItemResponseDto(
                UUID.randomUUID(), 2, orderId, productId, null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemMapper.toEntity(request)).thenReturn(item);
        when(orderItemRepository.save(item)).thenReturn(item);
        when(orderItemMapper.toResponse(item)).thenReturn(response);

        assertSame(response, orderService.addItem(orderId, request));
        assertSame(order, item.getOrder());
        assertSame(product, item.getProduct());
    }

    @Test
    void addItemRejectsUnknownOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.addItem(orderId, new AddOrderItemRequestDto(productId, 1)));
        verify(productRepository, never()).findById(any());
    }

    @Test
    void addItemRejectsOrderThatIsNotDraft() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, false, false)));

        assertThrows(OrderNotDraftException.class,
                () -> orderService.addItem(orderId, new AddOrderItemRequestDto(productId, 1)));
        verify(productRepository, never()).findById(any());
    }

    @Test
    void addItemRejectsUnknownProduct() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, true, false)));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderService.addItem(orderId, new AddOrderItemRequestDto(productId, 1)));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void addItemRejectsDisabledProduct() {
        Product product = new Product();
        product.setDisabled(true);
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, true, false)));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(ProductInactiveException.class,
                () -> orderService.addItem(orderId, new AddOrderItemRequestDto(productId, 1)));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void sendOrderRequiresAtLeastOneItem() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, true, false)));
        when(orderItemRepository.findAllByOrder_Id(orderId)).thenReturn(List.of());

        assertThrows(OrderEmptyException.class, () -> orderService.sendOrder(orderId));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void sendOrderChangesDraftToFalseAndReturnsItems() {
        Order order = order(orderId, true, false);
        OrderItem item = new OrderItem();
        OrderDetailItemResponseDto itemResponse =
                new OrderDetailItemResponseDto(UUID.randomUUID(), 1, null);
        OrderDetailResponseDto response =
                new OrderDetailResponseDto(orderId, 4, "Maria", false, false, null, List.of(itemResponse));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrder_Id(orderId)).thenReturn(List.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemMapper.toDetailResponse(item)).thenReturn(itemResponse);
        when(orderMapper.toDetailResponse(order, List.of(itemResponse))).thenReturn(response);

        assertSame(response, orderService.sendOrder(orderId));
        assertFalse(order.isDraft());
    }

    @Test
    void sendOrderRejectsAlreadySentOrder() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, false, false)));

        assertThrows(OrderNotDraftException.class, () -> orderService.sendOrder(orderId));
        verify(orderItemRepository, never()).findAllByOrder_Id(orderId);
    }

    @Test
    void finishOrderRequiresPreviouslySentOrder() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, true, false)));

        assertThrows(OrderNotSentException.class, () -> orderService.finishOrder(orderId));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void finishOrderRejectsAlreadyFinishedOrder() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, false, true)));

        assertThrows(OrderAlreadyFinishedException.class,
                () -> orderService.finishOrder(orderId));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void finishOrderSetsStatusAndReturnsDetails() {
        Order order = order(orderId, false, false);
        OrderDetailResponseDto response =
                new OrderDetailResponseDto(orderId, 4, "Maria", true, false, null, List.of());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemRepository.findAllByOrder_Id(orderId)).thenReturn(List.of());
        when(orderMapper.toDetailResponse(order, List.of())).thenReturn(response);

        assertSame(response, orderService.finishOrder(orderId));
        assertTrue(order.isStatus());
    }

    @Test
    void removeItemDeletesItemBelongingToDraftOrder() {
        Order order = order(orderId, true, false);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(productId)).thenReturn(Optional.of(item));

        orderService.removeItem(orderId, productId);

        verify(orderItemRepository).delete(item);
    }

    @Test
    void removeItemRejectsItemFromDifferentOrder() {
        Order itemOrder = order(UUID.randomUUID(), true, false);
        OrderItem item = new OrderItem();
        item.setOrder(itemOrder);
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, true, false)));
        when(orderItemRepository.findById(productId)).thenReturn(Optional.of(item));

        assertThrows(OrderItemNotFoundException.class,
                () -> orderService.removeItem(orderId, productId));
        verify(orderItemRepository, never()).delete(any());
    }

    @Test
    void deleteOnlyAllowsDraftOrders() {
        Order order = order(orderId, true, false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.delete(orderId);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteRejectsSentOrders() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order(orderId, false, false)));

        assertThrows(OrderNotDraftException.class, () -> orderService.delete(orderId));
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void findAllReturnsPagedOrdersWithGroupedItems() {
        Order order = order(orderId, false, false);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        OrderDetailItemResponseDto itemResponse =
                new OrderDetailItemResponseDto(UUID.randomUUID(), 2, null);
        OrderDetailResponseDto detail =
                new OrderDetailResponseDto(orderId, 4, "Maria", false, false, null, List.of(itemResponse));
        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));
        when(orderItemRepository.findAllByOrder_IdIn(List.of(orderId))).thenReturn(List.of(item));
        when(orderItemMapper.toDetailResponse(item)).thenReturn(itemResponse);
        when(orderMapper.toDetailResponse(order, List.of(itemResponse))).thenReturn(detail);

        Page<OrderDetailResponseDto> result = orderService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(List.of(itemResponse), result.getContent().get(0).items());
        verify(orderItemRepository).findAllByOrder_IdIn(List.of(orderId));
    }

    private Order order(UUID id, boolean draft, boolean status) {
        Order order = new Order();
        order.setId(id);
        order.setDraft(draft);
        order.setStatus(status);
        order.setTableNumber(4);
        order.setName("Maria");
        return order;
    }
}
