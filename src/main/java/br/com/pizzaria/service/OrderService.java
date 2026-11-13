package br.com.pizzaria.service;

import br.com.pizzaria.dtos.OrderRequestDto;
import br.com.pizzaria.dtos.OrderResponseDto;
import br.com.pizzaria.entity.Order;
import br.com.pizzaria.mapper.OrderMapper;
import br.com.pizzaria.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    @Transactional
    public OrderResponseDto create(OrderRequestDto dto){
        Order order = orderMapper.toEntity(dto);
        Order saved = orderRepository.save(order);

        return orderMapper.toResponse(saved, List.of());
    }
}
