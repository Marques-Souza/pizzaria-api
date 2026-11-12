package br.com.pizzaria.service;

import br.com.pizzaria.dtos.ImageUploadResult;
import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.dtos.ProductUpdateDto;
import br.com.pizzaria.entity.Category;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.exception.CategoryInactiveException;
import br.com.pizzaria.exception.CategoryNotFoundException;
import br.com.pizzaria.exception.ProductAlreadyExistsException;
import br.com.pizzaria.exception.ProductNotFoundException;
import br.com.pizzaria.exception.ProductUpdateEmptyException;
import br.com.pizzaria.mapper.ProductMapper;
import br.com.pizzaria.repository.CategoryRepository;
import br.com.pizzaria.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAll(Pageable pageable, UUID categoryId){
        Page<Product> products;

        if (categoryId == null){
            products = productRepository.findAllByDisabledFalse(pageable);
        }else {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found."));

            if (!category.isActive()){
                throw new CategoryInactiveException("Category is inactive.");
            }

            products = productRepository
                    .findAllByDisabledFalseAndCategory_id(categoryId, pageable);
        }
        return products.map(productMapper::toResponse);
    }

    @Transactional
    public ProductResponseDto update(UUID id, ProductUpdateDto dto, MultipartFile file){
        boolean hasFile = file != null && !file.isEmpty();
        if (dto.name() == null
                && dto.description() == null
                && dto.price() == null
                && dto.categoryId() == null
                && !hasFile) {
            throw new ProductUpdateEmptyException("At least one product field or image must be provided.");
        }

        Product product = productRepository.findById(id)
                .filter(existing -> !existing.isDisabled())
                .orElseThrow(() -> new ProductNotFoundException("Active product not found."));

        if (dto.name() != null
                && productRepository.existsByNameIgnoreCaseAndIdNot(dto.name(), id)) {
            throw new ProductAlreadyExistsException("Product name already registered.");
        }

        Category category = null;
        if (dto.categoryId() != null) {
            category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found."));
            if (!category.isActive()) {
                throw new CategoryInactiveException("Category is inactive.");
            }
        }

        productMapper.update(dto, product);
        if (category != null){
            product.setCategory(category);
        }

        String oldBannerPublicId = product.getBannerPublicId();
        ImageUploadResult newImage = null;
        if (hasFile) {
            newImage = imageService.store(file, "products");
            product.setBanner(newImage.url());
            product.setBannerPublicId(newImage.publicId());
        }

        Product saved;
        try {
            saved = productRepository.saveAndFlush(product);
        } catch (RuntimeException ex) {
            if (newImage != null) {
                try {
                    imageService.delete(newImage.publicId());
                } catch (RuntimeException cleanupException) {
                    log.warn("Could not delete newly uploaded image after product update failed.", cleanupException);
                }
            }
            throw ex;
        }

        if (newImage != null
                && oldBannerPublicId != null
                && !oldBannerPublicId.equals(newImage.publicId())) {
            String publicIdToDelete = oldBannerPublicId;
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                imageService.delete(publicIdToDelete);
                            } catch (RuntimeException ex) {
                                log.warn("Product updated, but old image could not be deleted.", ex);
                            }
                        }
                    }
            );
        }

        return productMapper.toResponse(saved);
    }

}
