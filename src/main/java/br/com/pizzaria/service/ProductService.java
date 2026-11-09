package br.com.pizzaria.service;

import br.com.pizzaria.dtos.ImageUploadResult;
import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.entity.Category;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.exception.CategoryInactiveException;
import br.com.pizzaria.exception.CategoryNotFoundException;
import br.com.pizzaria.exception.ProductAlreadyExistsException;
import br.com.pizzaria.exception.ProductNotFoundException;
import br.com.pizzaria.mapper.ProductMapper;
import br.com.pizzaria.repository.CategoryRepository;
import br.com.pizzaria.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ImageService imageService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponseDto create(ProductRequestDto dto, MultipartFile file){
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found."));
        if (!category.isActive()){
            throw  new CategoryInactiveException("Category is inactive.");
        }

        if (productRepository.existsByNameIgnoreCase(dto.name())){
            throw new ProductAlreadyExistsException("Product name already registered.");
        }

        Product product = productMapper.toEntity(dto);
        ImageUploadResult image = imageService.store(file, "products");
        product.setCategory(category);
        product.setBanner(image.url());
        product.setBannerPublicId(image.publicId());

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto findById(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
        return productMapper.toResponse(product);
    }

}
