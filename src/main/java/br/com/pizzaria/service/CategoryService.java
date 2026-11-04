package br.com.pizzaria.service;

import br.com.pizzaria.mapper.CategoryMapper;
import br.com.pizzaria.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

}
