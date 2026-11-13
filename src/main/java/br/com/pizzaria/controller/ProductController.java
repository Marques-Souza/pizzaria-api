package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.dtos.ProductUpdateDto;
import br.com.pizzaria.service.ProductService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "Produtos", description = "Cadastro, consulta e gerenciamento de produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Criar produto", description = "Cria produto com imagem multipart. Requer papel ADMIN.")
    public ResponseEntity<ProductResponseDto> create(
            @ModelAttribute @Valid ProductRequestDto dto,
            @RequestPart("file")MultipartFile file){
        ProductResponseDto response = productService.create(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable UUID id){
        ProductResponseDto response = productService.findById(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(summary = "Listar produtos", description = "Lista produtos ativos de forma paginada; pode filtrar por categoryId.")
    public ResponseEntity<Page<ProductResponseDto>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false) UUID categoryId){
        return ResponseEntity.ok(productService.findAll(pageable, categoryId));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar produto", description = "Atualiza campos e/ou imagem usando multipart. Requer papel ADMIN.")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable UUID id,
            @ModelAttribute @Valid ProductUpdateDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ProductResponseDto response = productService.update(id, dto, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar produto", description = "Desativa logicamente o produto. Requer papel ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/reactivate")
    @Operation(summary = "Reativar produto", description = "Reativa um produto. Requer papel ADMIN.")
    public ResponseEntity<ProductResponseDto> reactivate(@PathVariable UUID id){
        ProductResponseDto response = productService.reactivate(id);
        return ResponseEntity.ok(response);
    }
}
