package br.com.pizzaria.service;

import br.com.pizzaria.mapper.OrderMapper;
import br.com.pizzaria.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
}
