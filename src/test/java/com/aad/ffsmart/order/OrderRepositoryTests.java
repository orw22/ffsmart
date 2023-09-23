package com.aad.ffsmart.order;

import com.aad.ffsmart.db.MongoConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ContextConfiguration(classes = MongoConfig.class)
@ExtendWith(SpringExtension.class)
class OrderRepositoryTests {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void givenOrders_whenFindApprovedOrders_thenApprovedOrdersReturned() {
        Flux<Order> orderFlux = orderRepository.findApproved();

        StepVerifier
                .create(orderFlux)
                .assertNext(order -> {
                    assertNotNull(order.getId());
                    assertEquals(OrderStatus.APPROVED, order.getStatus());
                    assertFalse(order.getItems().isEmpty());
                });
    }

    @Test
    void givenOrders_whenFindReadyOrders_thenReadyOrdersReturned() {
        Flux<Order> orderFlux = orderRepository.findReady();

        StepVerifier
                .create(orderFlux)
                .assertNext(order -> {
                    assertNotNull(order.getId());
                    assertEquals(OrderStatus.READY, order.getStatus());
                    assertFalse(order.getItems().isEmpty());
                });
    }

    @Test
    void givenOrders_whenFindByDriverId_thenDriverOrdersReturned() {
        String driverId = "63d837341d955227f5fa9a8a";
        Flux<Order> orderFlux = orderRepository.findByDriverId(driverId);

        StepVerifier
                .create(orderFlux)
                .assertNext(order -> {
                    assertNotNull(order.getId());
                    assertEquals(driverId, order.getDriverId());
                });
    }
}
