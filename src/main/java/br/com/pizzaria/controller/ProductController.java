package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.dtos.ProductUpdateDto;
import br.com.pizzaria.service.ProductService;
import jakarta.validation.Valid;
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
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> create(
            @ModelAttribute @Valid ProductRequestDto dto,
            @RequestPart("file")MultipartFile file){
        ProductResponseDto response = productService.create(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable UUID id){
        ProductResponseDto response = productService.findById(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false) UUID categoryId){
        return ResponseEntity.ok(productService.findAll(pageable, categoryId));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable UUID id,
            @ModelAttribute @Valid ProductUpdateDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ProductResponseDto response = productService.update(id, dto, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ProductResponseDto> reactivate(@PathVariable UUID id){
        ProductResponseDto response = productService.reactivate(id);
        return ResponseEntity.ok(response);
    }
}
