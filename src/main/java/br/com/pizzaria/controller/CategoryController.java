package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(
           @Valid @RequestBody CategoryRequestDto dto){
       CategoryResponseDto response = categoryService.create(dto);
       return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
