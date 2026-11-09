package br.com.pizzaria.service;

import br.com.pizzaria.mapper.ProductMapper;
import br.com.pizzaria.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ImageService imageService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
}
