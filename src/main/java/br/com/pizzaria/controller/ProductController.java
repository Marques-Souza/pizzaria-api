package br.com.pizzaria.controller;

import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
