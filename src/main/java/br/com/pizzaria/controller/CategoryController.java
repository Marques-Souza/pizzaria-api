package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.CategoryRequestDto;
import br.com.pizzaria.dtos.CategoryResponseDto;
import br.com.pizzaria.dtos.CategoryUpdateDto;
import br.com.pizzaria.repository.CategoryRepository;
import br.com.pizzaria.service.CategoryService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Tag(name = "Categorias", description = "Cadastro e consulta de categorias de produtos")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma categoria. Requer papel ADMIN.")
    public ResponseEntity<CategoryResponseDto> create(
           @Valid @RequestBody CategoryRequestDto dto){
       CategoryResponseDto response = categoryService.create(dto);
       return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoryResponseDto> findById(@PathVariable UUID id){
        CategoryResponseDto response = categoryService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar categorias")
    public  ResponseEntity<List<CategoryResponseDto>> findAll(){
        List<CategoryResponseDto> response = categoryService.findAll();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria. Requer papel ADMIN.")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateDto dto){
        CategoryResponseDto response = categoryService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar categoria", description = "Desativa uma categoria. Requer papel ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
