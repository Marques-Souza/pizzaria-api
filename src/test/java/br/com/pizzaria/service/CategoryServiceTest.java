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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createRejectsDuplicateName() {
        CategoryRequestDto request = new CategoryRequestDto("Pizzas", "Pizzas salgadas");
        when(categoryRepository.existsByName(request.name())).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.create(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createSavesAndMapsCategory() {
        CategoryRequestDto request = new CategoryRequestDto("Pizzas", "Pizzas salgadas");
        Category category = new Category();
        CategoryResponseDto response = new CategoryResponseDto(
                UUID.randomUUID(), "Pizzas", "Pizzas salgadas", true);
        when(categoryRepository.existsByName(request.name())).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        assertSame(response, categoryService.create(request));
        verify(categoryRepository).save(category);
    }

    @Test
    void findByIdRejectsMissingCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(id));
    }

    @Test
    void updateRejectsInactiveCategory() {
        UUID id = UUID.randomUUID();
        Category category = new Category();
        category.setActive(false);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        assertThrows(CategoryInactiveException.class,
                () -> categoryService.update(id, new CategoryUpdateDto("Nova", null)));
        verify(categoryMapper, never()).update(any(), any());
    }

    @Test
    void deleteMarksCategoryInactive() {
        UUID id = UUID.randomUUID();
        Category category = new Category();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.delete(id);

        org.junit.jupiter.api.Assertions.assertFalse(category.isActive());
        verify(categoryRepository).save(category);
    }
}
