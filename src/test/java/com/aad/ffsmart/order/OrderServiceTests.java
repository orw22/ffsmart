package com.aad.ffsmart.order;

import com.aad.ffsmart.alert.Alert;
import com.aad.ffsmart.inventory.InventoryService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {
    private static Order order;
    private static Order readyOrder;
    private static Order inTransitOrder;
    private static Order deliveredOrder;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeAll
    static void setup() {
        readyOrder = new Order(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                OrderStatus.READY,
                new Date(),
                new Date(),
                List.of()
        );
        order = new Order(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                OrderStatus.APPROVED,
                new Date(),
                new Date(),
                List.of()
        );
        inTransitOrder = new Order(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                OrderStatus.IN_TRANSIT,
                new Date(),
                new Date(),
                List.of()
        );
        deliveredOrder = new Order(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                OrderStatus.DELIVERED,
                new Date(),
                new Date(),
                List.of()
        );
    }

    @DisplayName("Create order, expect order created and returned")
    @Test
    void givenUser_whenCreateOrder_thenOrderCreated() {
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        Mono<Order> orderMono = orderService.createOrder(order, false);

        StepVerifier
                .create(orderMono)
                .assertNext(ordr -> assertEquals(order, ordr))
                .verifyComplete();

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Alert.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @DisplayName("Get all orders, expect orders returned")
    @Test
    void givenNoStatus_whenGetAllOrders_thenOrdersReturned() {
        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        Flux<Order> orderFlux = orderService.getAllOrders(null);

        StepVerifier
                .create(orderFlux)
                .assertNext(ordr -> assertEquals(order, ordr))
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
    }

    @DisplayName("Get all orders with status approved, expect approved orders returned")
    @Test
    void givenStatusApproved_whenGetAllOrders_thenApprovedOrdersReturned() {
        when(orderRepository.findAll(any(OrderStatus.class))).thenReturn(Flux.just(order));
        Flux<Order> orderFlux = orderService.getAllOrders(OrderStatus.APPROVED);

        StepVerifier
                .create(orderFlux)
                .assertNext(ordr -> assertEquals(OrderStatus.APPROVED, ordr.getStatus()))
                .verifyComplete();

        verify(orderRepository, times(1)).findAll(any(OrderStatus.class));
    }

    @DisplayName("Get my orders with user id, expect orders returned")
    @Test
    void givenOrders_whenGetMyOrders_thenOrdersReturned() {
        when(orderRepository.findByDriverId(anyString())).thenReturn(Flux.just(order));
        Flux<Order> orderFlux = orderService.getMyOrders("123");

        StepVerifier
                .create(orderFlux)
                .assertNext(ordr -> assertEquals(order, ordr))
                .verifyComplete();

        verify(orderRepository, times(1)).findByDriverId(anyString());
    }

    @DisplayName("Get ready orders, expect orders returned")
    @Test
    void givenOrders_whenGetReadyOrders_thenOrdersReturned() {
        when(orderRepository.findReady()).thenReturn(Flux.just(readyOrder));
        Flux<Order> orderFlux = orderService.getReadyOrders();

        StepVerifier
                .create(orderFlux)
                .assertNext(ordr -> assertEquals(OrderStatus.READY, ordr.getStatus()))
                .verifyComplete();

        verify(orderRepository, times(1)).findReady();
    }

    @DisplayName("Get approved orders, expect orders returned")
    @Test
    void givenOrders_whenGetApprovedOrders_thenOrdersReturned() {
        when(orderRepository.findApproved()).thenReturn(Flux.just(order));
        Flux<Order> orderFlux = orderService.getApprovedOrders();

        StepVerifier
                .create(orderFlux)
                .assertNext(ordr -> assertEquals(OrderStatus.APPROVED, ordr.getStatus()))
                .verifyComplete();

        verify(orderRepository, times(1)).findApproved();
    }

    @DisplayName("Get order by id, expect order returned")
    @Test
    void givenOrders_whenGetOrderById_thenOrderReturned() {
        when(orderRepository.findById(anyString())).thenReturn(Mono.just(order));
        Mono<Order> orderMono = orderService.getOrderById("123");

        StepVerifier
                .create(orderMono)
                .assertNext(ordr -> assertEquals(order, ordr))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(anyString());
    }

    @DisplayName("Get order with invalid id, expect order not found error")
    @Test
    void givenInvalidOrderId_whenGetOrderById_thenError() {
        when(orderRepository.findById(anyString())).thenReturn(Mono.empty());
        Mono<Order> orderMono = orderService.getOrderById("123");

        StepVerifier
                .create(orderMono)
                .expectError()
                .verify();
    }

    @DisplayName("Approve order, expect order status approved")
    @Test
    void givenOrder_whenApproveOrder_thenOrderStatusApproved() {
        when(orderRepository.findById(anyString())).thenReturn(Mono.just(readyOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        Mono<Order> orderMono = orderService.approveOrder("123");

        StepVerifier
                .create(orderMono)
                .assertNext(ordr -> assertEquals(OrderStatus.APPROVED, ordr.getStatus()))
                .verifyComplete();

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Alert.class));
        verify(orderRepository, times(1)).findById(anyString());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @DisplayName("Reject order, expect order deleted")
    @Test
    void givenOrder_whenRejectOrder_thenOrderDeleted() {
        when(orderRepository.findById(anyString())).thenReturn(Mono.just(readyOrder));
        when(orderRepository.delete(any(Order.class))).thenReturn(Mono.empty());

        orderService.rejectOrder("123").block();

        verify(orderRepository, times(1)).findById(anyString());
        verify(orderRepository, times(1)).delete(any(Order.class));
    }

    @DisplayName("Dispatch order, expect order status in transit")
    @Test
    void givenOrder_whenDispatchOrder_thenOrderStatusInTransit() {
        when(orderRepository.findById(anyString())).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(inTransitOrder));
        Mono<Order> orderMono = orderService.dispatchOrder("123", "789");

        StepVerifier
                .create(orderMono)
                .assertNext(ordr -> assertEquals(OrderStatus.IN_TRANSIT, ordr.getStatus()))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(anyString());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @DisplayName("Deliver order, expect order status delivered")
    @Test
    void givenOrder_whenDeliverOrder_thenOrderStatusDelivered() {
        when(orderRepository.findById(anyString())).thenReturn(Mono.just(inTransitOrder));
        when(orderRepository.save(any())).thenReturn(Mono.just(deliveredOrder));
        lenient().when(inventoryService.addInventory(any(), any())).thenReturn(Mono.empty());
        Mono<Order> orderMono = orderService.deliverOrder("123");

        StepVerifier
                .create(orderMono)
                .assertNext(ordr -> assertEquals(OrderStatus.DELIVERED, ordr.getStatus()))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(anyString());
        verify(orderRepository, times(1)).save(any());
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), any(Alert.class));
    }
}
