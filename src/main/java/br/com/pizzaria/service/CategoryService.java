package br.com.pizzaria.service;

import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.dtos.CategoryUpdateDto;
import br.com.pizzaria.entity.Category;
import br.com.pizzaria.exception.CategoryAlreadyExistsException;
import br.com.pizzaria.exception.CategoryInactiveException;
import br.com.pizzaria.exception.CategoryNotFoundException;
import br.com.pizzaria.mapper.CategoryMapper;
import br.com.pizzaria.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public CategoryResponseDto findById(UUID id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found."));
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAll(){
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponseDto update(UUID id, CategoryUpdateDto dto){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found."));
        if (!category.isActive()){
            throw new CategoryInactiveException("Category is inactive.");
        }
        categoryMapper.update(dto, category);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found."));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
