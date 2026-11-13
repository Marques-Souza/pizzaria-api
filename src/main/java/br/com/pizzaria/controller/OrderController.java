package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.*;
import br.com.pizzaria.service.OrderService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Pedidos", description = "Criacao, consulta e fluxo de estado dos pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;


    @PostMapping
    @Operation(summary = "Criar pedido em rascunho")
    public ResponseEntity<OrderResponseDto> create(
           @Valid @RequestBody OrderRequestDto dto){
        OrderResponseDto response = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    @PostMapping("/{orderId}/items")
    @Operation(summary = "Adicionar item ao pedido", description = "Adiciona produto ativo a um pedido em rascunho.")
    public ResponseEntity<OrderItemResponseDto> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddOrderItemRequestDto dto){
        OrderItemResponseDto response = orderService.addItem(orderId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna o pedido com seus itens e dados dos produtos.")
    public ResponseEntity<OrderDetailResponseDto> findOrderById(@PathVariable UUID orderId){
        OrderDetailResponseDto response = orderService.findOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Remover item do pedido", description = "Remove um item de um pedido em rascunho.")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId){
        orderService.removeItem(orderId, itemId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{orderId}/send")
    @Operation(summary = "Enviar pedido para preparo", description = "Envia pedido com pelo menos um item e encerra o rascunho.")
    public ResponseEntity<OrderDetailResponseDto> sendOrder(@PathVariable UUID orderId){
        OrderDetailResponseDto response = orderService.sendOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/finish")
    @Operation(summary = "Finalizar pedido", description = "Finaliza um pedido que ja foi enviado para preparo.")
    public ResponseEntity<OrderDetailResponseDto> finishOrder(@PathVariable UUID orderId) {
        OrderDetailResponseDto response = orderService.finishOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Retorna pedidos paginados com seus itens.")
    public ResponseEntity<Page<OrderDetailResponseDto>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Excluir pedido em rascunho", description = "Exclui pedido somente enquanto ainda estiver em rascunho.")
    public ResponseEntity<Void> delete(@PathVariable UUID orderId) {
        orderService.delete(orderId);
        return ResponseEntity.noContent().build();
    }
}
