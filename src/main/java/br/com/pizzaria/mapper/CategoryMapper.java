package br.com.pizzaria.mapper;

import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.dtos.CategoryUpdateDto;
import br.com.pizzaria.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequestDto dto);

    CategoryResponseDto toResponse(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(CategoryUpdateDto dto, @MappingTarget Category category);
}
