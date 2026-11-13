package br.com.pizzaria;

import br.com.pizzaria.entity.Category;
import br.com.pizzaria.entity.Order;
import br.com.pizzaria.entity.OrderItem;
import br.com.pizzaria.entity.Product;
import br.com.pizzaria.repository.CategoryRepository;
import br.com.pizzaria.repository.OrderItemRepository;
import br.com.pizzaria.repository.OrderRepository;
import br.com.pizzaria.repository.ProductRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class PostgreSqlIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("security.jwt.secret", () -> "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        registry.add("cloudinary.url", () -> "cloudinary://test:test@pizzaria");
    }

    @Test
    void appliesAllMigrationsAndCreatesExpectedTables() {
        assertEquals(6, flyway.info().applied().length);
        assertTrue(tableExists("categories"));
        assertTrue(tableExists("users"));
        assertTrue(tableExists("products"));
        assertTrue(tableExists("orders"));
        assertTrue(tableExists("order_items"));
        assertTrue(columnExists("products", "banner_public_id"));
    }

    @Test
    void persistsProductAndOrderRelationshipsAndCascadesOrderDeletion() {
        Category category = new Category();
        category.setName("Categoria integração");
        category.setDescription("Categoria criada no PostgreSQL de teste");
        category = categoryRepository.saveAndFlush(category);
        assertNotNull(category.getId());

        Product product = new Product();
        product.setName("Pizza integração");
        product.setDescription("Produto para teste integrado");
        product.setPrice(new BigDecimal("42.90"));
        product.setCategory(category);
        product.setBanner("https://example.test/pizza.jpg");
        product = productRepository.saveAndFlush(product);

        Order order = new Order();
        order.setTableNumber(7);
        order.setName("Pedido de integração");
        order = orderRepository.saveAndFlush(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setAmount(2);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItemRepository.saveAndFlush(orderItem);

        List<OrderItem> loadedItems = orderItemRepository.findAllByOrder_Id(order.getId());
        assertEquals(1, loadedItems.size());
        assertEquals(product.getId(), loadedItems.get(0).getProduct().getId());
        assertEquals("Pizza integração", loadedItems.get(0).getProduct().getName());
        assertEquals(0, new BigDecimal("42.90").compareTo(
                loadedItems.get(0).getProduct().getPrice()));
        assertTrue(productRepository.existsByNameIgnoreCase("PIZZA INTEGRAÇÃO"));

        orderRepository.deleteById(order.getId());
        orderRepository.flush();

        assertTrue(orderItemRepository.findAllByOrder_Id(order.getId()).isEmpty());
        assertFalse(productRepository.findById(product.getId()).isEmpty());
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select to_regclass(?) is not null",
                Boolean.class,
                "public." + tableName));
    }

    private boolean columnExists(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                """
                select exists (
                    select 1
                    from information_schema.columns
                    where table_schema = 'public'
                      and table_name = ?
                      and column_name = ?
                )
                """,
                Boolean.class,
                tableName,
                columnName));
    }
}
