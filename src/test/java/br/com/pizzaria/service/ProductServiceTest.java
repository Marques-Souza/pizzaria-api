package br.com.pizzaria.service;

import br.com.pizzaria.dtos.ImageUploadResult;
import br.com.pizzaria.dtos.ProductRequestDto;
import br.com.pizzaria.dtos.ProductResponseDto;
import br.com.pizzaria.dtos.ProductUpdateDto;
import br.com.pizzaria.entity.Category;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.exception.CategoryInactiveException;
import br.com.pizzaria.exception.ProductAlreadyExistsException;
import br.com.pizzaria.exception.ProductUpdateEmptyException;
import br.com.pizzaria.mapper.ProductMapper;
import br.com.pizzaria.repository.CategoryRepository;
import br.com.pizzaria.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ImageService imageService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MultipartFile imageFile;
    @InjectMocks
    private ProductService productService;

    @Test
    void createRejectsInactiveCategoryBeforeUploadingImage() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setActive(false);
        ProductRequestDto request = productRequest(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThrows(CategoryInactiveException.class,
                () -> productService.create(request, imageFile));

        verify(imageService, never()).store(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateProductNameBeforeUploadingImage() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(new Category()));
        when(productRepository.existsByNameIgnoreCase("Coca-Cola")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class,
                () -> productService.create(productRequest(categoryId), imageFile));

        verify(imageService, never()).store(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createAssociatesCategoryAndUploadedImage() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        Product product = new Product();
        ProductRequestDto request = productRequest(categoryId);
        ProductResponseDto response = productResponse(categoryId);
        ImageUploadResult uploadedImage =
                new ImageUploadResult("https://images.test/product.webp", "products/one");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsByNameIgnoreCase("Coca-Cola")).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(product);
        when(imageService.store(imageFile, "products")).thenReturn(uploadedImage);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        assertSame(response, productService.create(request, imageFile));

        assertSame(category, product.getCategory());
        assertEquals(uploadedImage.url(), product.getBanner());
        assertEquals(uploadedImage.publicId(), product.getBannerPublicId());
    }

    @Test
    void findAllUsesActiveProductsPageWhenNoCategoryFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        ProductResponseDto response = productResponse(UUID.randomUUID());
        when(productRepository.findAllByDisabledFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponseDto> result = productService.findAll(pageable, null);

        assertEquals(List.of(response), result.getContent());
        verify(productRepository).findAllByDisabledFalse(pageable);
    }

    @Test
    void findAllRejectsInactiveCategoryFilter() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setActive(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThrows(CategoryInactiveException.class,
                () -> productService.findAll(PageRequest.of(0, 10), categoryId));
        verify(productRepository, never())
                .findAllByDisabledFalseAndCategory_id(any(), any());
    }

    @Test
    void updateRejectsEmptyPatch() {
        ProductUpdateDto request = new ProductUpdateDto(null, null, null, null);

        assertThrows(ProductUpdateEmptyException.class,
                () -> productService.update(UUID.randomUUID(), request, null));
        verify(productRepository, never()).findById(any());
    }

    @Test
    void deleteMarksProductDisabled() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.delete(productId);

        assertTrue(product.isDisabled());
        verify(productRepository).save(product);
    }

    @Test
    void reactivateRejectsProductWhoseCategoryIsInactive() {
        UUID productId = UUID.randomUUID();
        Category category = new Category();
        category.setActive(false);
        Product product = new Product();
        product.setCategory(category);
        product.setDisabled(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(CategoryInactiveException.class,
                () -> productService.reactivate(productId));
        assertTrue(product.isDisabled());
        verify(productRepository, never()).save(product);
    }

    private ProductRequestDto productRequest(UUID categoryId) {
        return new ProductRequestDto(
                "Coca-Cola",
                "Refrigerante",
                new BigDecimal("10.50"),
                categoryId
        );
    }

    private ProductResponseDto productResponse(UUID categoryId) {
        return new ProductResponseDto(
                UUID.randomUUID(),
                "Coca-Cola",
                "Refrigerante",
                new BigDecimal("10.50"),
                categoryId,
                "Bebidas",
                "https://images.test/product.webp",
                false
        );
    }
}
