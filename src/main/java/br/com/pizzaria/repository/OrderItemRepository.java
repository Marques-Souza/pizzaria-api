package br.com.pizzaria.repository;

import br.com.pizzaria.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @EntityGraph(attributePaths = "product")
    List<OrderItem> findAllByOrder_Id(UUID orderId);
    @EntityGraph(attributePaths = "product")
    List<OrderItem> findAllByOrder_IdIn(Collection<UUID> orderIds);
}
