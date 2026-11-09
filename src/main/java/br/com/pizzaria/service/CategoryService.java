package br.com.pizzaria.service;

import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.entity.Category;
import br.com.pizzaria.exception.CategoryAlreadyExistsException;
import br.com.pizzaria.mapper.CategoryMapper;
import br.com.pizzaria.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Transactional
    public CategoryResponseDto create(CategoryRequestDto dto){
        if (categoryRepository.existsByName(dto.name())){
            throw new CategoryAlreadyExistsException("Category already registered.");
        }
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    public List<CategoryResponseDto> findAll(){
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
