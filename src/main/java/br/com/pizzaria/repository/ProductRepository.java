package br.com.pizzaria.repository;

import br.com.pizzaria.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Page<Product> findAllByDisabledFalse(Pageable pageable);

    Page<Product> findAllByDisabledFalseAndCategory_id(UUID categoryId, Pageable pageable);
}
