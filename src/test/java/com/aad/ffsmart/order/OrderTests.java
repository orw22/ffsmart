package com.aad.ffsmart.order;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.inventory.InventoryChangeRepository;
import com.aad.ffsmart.inventory.InventoryItem;
import com.aad.ffsmart.user.User;
import com.aad.ffsmart.user.UserRepository;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class OrderTests {
    private static ParseContext jsonPath;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private InventoryChangeRepository inventoryChangeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;

    @BeforeAll
    static void setup() {
        Configuration jacksonConfig = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider())
                .jsonProvider(new JacksonJsonProvider())
                .build();
        jsonPath = JsonPath.using(jacksonConfig);
    }

    @DisplayName("Create order integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUserAndContext_whenCreateOrder_thenOrderAddedToDatabase() {
//        test will fail if rabbitmq is not running locally
        String json = webTestClient.post()
                .uri("/orders")
                .body(Mono.just(new Order(
                        "63d1b3dae8b8e7e8b68300af",
                        "Supplier 1",
                        OrderStatus.READY,
                        new Date(),
                        new Date(),
                        List.of(new InventoryItem("0", "Bananas 100g", "63d1b3dae8b8e7e8b68300af", "Supplier 1", 5, new Date()))
                )), Order.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .consumeWith(System.out::println)
                .returnResult()
                .getResponseBody();

        Order res = jsonPath.parse(json).read("$.data", Order.class);

        Mono<Order> orderMono = orderRepository.findById(res.getId());
        StepVerifier
                .create(orderMono)
                .consumeNextWith(order -> {
                    assertEquals(OrderStatus.APPROVED, order.getStatus());
                    assertEquals("63d1b3dae8b8e7e8b68300af", order.getSupplierId());
                    assertFalse(order.getItems().isEmpty());
                })
                .verifyComplete();
    }

    @DisplayName("Get all orders integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenOrders_whenGetAllOrders_thenStatusOkAndOrdersReturned() {
        Order firstOrder = orderRepository.findAll()
                .sort(Comparator.comparing(Order::getPlacedDate).reversed()).blockFirst();

        assert firstOrder != null;
        webTestClient.mutate()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build().get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].supplierId").isEqualTo(firstOrder.getSupplierId())
                .jsonPath("$.data[0].status").isEqualTo(firstOrder.getStatus().value)
                .jsonPath("$.data[0].items").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @DisplayName("Get my orders integration test")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void givenOrders_whenGetMyOrders_thenStatusOkAndOrdersReturned() {
        String driverId = "63d837341d955227f5fa9a8a";
        User user = userRepository.findById(driverId).block();
        String token = "Bearer " + jwtUtil.generateToken(user);
        Order order = orderRepository.findByDriverId(driverId).blockFirst();

        assert order != null;
        webTestClient.get()
                .uri("/orders/my-orders")
                .header("Authorization", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].driverId").isEqualTo(driverId)
                .consumeWith(System.out::println);
    }

    @DisplayName("Get approved orders integration test")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void givenOrders_whenGetReadyOrders_thenStatusOkAndReadyOrdersReturned() {
        List<Order> order = orderRepository.findApproved().collectList().block();

        assert order != null;
        webTestClient.get()
                .uri("/orders/approved")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(order.get(0).getId())
                .jsonPath("$.data[0].status").isEqualTo(OrderStatus.APPROVED.value)
                .jsonPath("$.data[1].id").isEqualTo(order.get(1).getId())
                .jsonPath("$.data[1].status").isEqualTo(OrderStatus.APPROVED.value)
                .consumeWith(System.out::println);
    }

    @DisplayName("Get order by id integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenOrders_whenGetOrderById_thenOrderReturned() {
        String orderId = "63d7bd4710d6a64fc34b900b";
        Order order = orderRepository.findById(orderId).block();

        assert order != null;
        webTestClient.get()
                .uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(order.getId())
                .jsonPath("$.data.supplierName").isEqualTo(order.getSupplierName())
                .jsonPath("$.data.items").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @DisplayName("Reject order integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenOrders_whenRejectOrder_thenOrderDeleted() {
        String orderId = "63d722f0ed4bea7f3853c424";

        webTestClient.delete()
                .uri("/orders/" + orderId + "/reject")
                .exchange()
                .expectStatus().isNoContent()
                .returnResult(String.class);

        Mono<Order> orderMono = orderRepository.findById(orderId);
        StepVerifier.create(orderMono).expectError(); // order not found
    }

    @DisplayName("Deliver order integration test")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void givenOrders_whenDeliverOrder_thenStatusOkAndItemsAddedToInventory() {
        int preInventoryChangeCount = Objects.requireNonNull(inventoryChangeRepository.findAll().collectList().block()).size();
        String orderId = "63d7147f61d1d178f5d86cca";

        webTestClient.put()
                .uri("/orders/" + orderId + "/deliver")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .returnResult();

        Mono<Order> orderMono = orderRepository.findById(orderId);
        StepVerifier
                .create(orderMono)
                .consumeNextWith(order -> {
                    assertEquals(OrderStatus.DELIVERED, order.getStatus());
                });
        int postInventoryChangeCount = Objects.requireNonNull(inventoryChangeRepository.findAll().collectList().block()).size();

        assertEquals(preInventoryChangeCount + 1, postInventoryChangeCount);
    }
}
