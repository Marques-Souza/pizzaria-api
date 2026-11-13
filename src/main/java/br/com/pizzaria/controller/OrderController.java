package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.*;
import br.com.pizzaria.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;


    @PostMapping
    public ResponseEntity<OrderResponseDto> create(
           @Valid @RequestBody OrderRequestDto dto){
        OrderResponseDto response = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemResponseDto> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddOrderItemRequestDto dto){
        OrderItemResponseDto response = orderService.addItem(orderId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> findOrderById(@PathVariable UUID orderId){
        OrderDetailResponseDto response = orderService.findOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId){
        orderService.removeItem(orderId, itemId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{orderId}/send")
    public ResponseEntity<OrderDetailResponseDto> sendOrder(@PathVariable UUID orderId){
        OrderDetailResponseDto response = orderService.sendOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/finish")
    public ResponseEntity<OrderDetailResponseDto> finishOrder(@PathVariable UUID orderId) {
        OrderDetailResponseDto response = orderService.finishOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
