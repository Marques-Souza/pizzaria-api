package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.AddOrderItemRequestDto;
import br.com.pizzaria.dtos.OrderDetailResponseDto;
import br.com.pizzaria.dtos.OrderItemResponseDto;
import br.com.pizzaria.dtos.OrderRequestDto;
import br.com.pizzaria.dtos.OrderResponseDto;
import br.com.pizzaria.exception.GlobalExceptionHandler;
import br.com.pizzaria.exception.OrderAlreadyFinishedException;
import br.com.pizzaria.exception.OrderNotFoundException;
import br.com.pizzaria.service.OrderService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private UUID orderId;
    private UUID itemId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        productId = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(
                        Validation.buildDefaultValidatorFactory().getValidator()))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createReturns201AndCreatedOrder() throws Exception {
        OrderResponseDto response =
                new OrderResponseDto(orderId, 5, "Maria", false, true, List.of());
        when(orderService.create(new OrderRequestDto(5, "Maria"))).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "table": 5,
                                  "name": "Maria"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.table").value(5))
                .andExpect(jsonPath("$.draft").value(true))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(orderService).create(new OrderRequestDto(5, "Maria"));
    }

    @Test
    void createReturns400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "table": 0,
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.table").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void addItemReturns201AndCreatedItem() throws Exception {
        OrderItemResponseDto response =
                new OrderItemResponseDto(itemId, 2, orderId, productId, null);
        when(orderService.addItem(orderId, new AddOrderItemRequestDto(productId, 2)))
                .thenReturn(response);

        mockMvc.perform(post("/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "%s",
                                  "amount": 2
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.amount").value(2))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.productId").value(productId.toString()));

        verify(orderService).addItem(orderId, new AddOrderItemRequestDto(productId, 2));
    }

    @Test
    void addItemReturns400WhenAmountIsNotPositive() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "%s",
                                  "amount": 0
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void findByIdReturnsOrderDetails() throws Exception {
        OrderDetailResponseDto response =
                new OrderDetailResponseDto(orderId, 5, "Maria", false, true, null, List.of());
        when(orderService.findOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.table").value(5))
                .andExpect(jsonPath("$.draft").value(true))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void findByIdReturns404WhenOrderDoesNotExist() throws Exception {
        when(orderService.findOrderById(orderId))
                .thenThrow(new OrderNotFoundException("Order not found."));

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order Not Found"))
                .andExpect(jsonPath("$.detail").value("Order not found."));
    }

    @Test
    void removeItemReturns204WithoutBody() throws Exception {
        doNothing().when(orderService).removeItem(orderId, itemId);

        mockMvc.perform(delete("/orders/{orderId}/items/{itemId}", orderId, itemId))
                .andExpect(status().isNoContent())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(""));

        verify(orderService).removeItem(orderId, itemId);
    }

    @Test
    void sendOrderReturnsUpdatedOrder() throws Exception {
        OrderDetailResponseDto response =
                new OrderDetailResponseDto(orderId, 5, "Maria", false, false, null, List.of());
        when(orderService.sendOrder(orderId)).thenReturn(response);

        mockMvc.perform(patch("/orders/{orderId}/send", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.draft").value(false));
    }

    @Test
    void finishOrderReturns409WhenOrderIsAlreadyFinished() throws Exception {
        when(orderService.finishOrder(orderId))
                .thenThrow(new OrderAlreadyFinishedException("Order has already been finished."));

        mockMvc.perform(patch("/orders/{orderId}/finish", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Order State Conflict"))
                .andExpect(jsonPath("$.detail").value("Order has already been finished."));
    }

    @Test
    void findAllReturnsPagedOrders() throws Exception {
        OrderDetailResponseDto order =
                new OrderDetailResponseDto(orderId, 5, "Maria", true, false, null, List.of());
        PageRequest pageRequest = PageRequest.of(0, 10, org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        when(orderService.findAll(pageRequest))
                .thenReturn(new PageImpl<>(List.of(order), pageRequest, 1));

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$.content[0].status").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deleteOrderReturns204WithoutBody() throws Exception {
        doNothing().when(orderService).delete(orderId);

        mockMvc.perform(delete("/orders/{orderId}", orderId))
                .andExpect(status().isNoContent())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(""));

        verify(orderService).delete(orderId);
    }
}
