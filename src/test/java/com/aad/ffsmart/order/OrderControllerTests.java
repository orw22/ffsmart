package com.aad.ffsmart.order;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.exception.GlobalExceptionHandler;
import com.aad.ffsmart.web.WebFluxTestSecurityConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(OrderController.class)
@Import(WebFluxTestSecurityConfig.class)
class OrderControllerTests {
    private static Order order;
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @MockBean
    private JWTUtil jwtUtil;

    @BeforeAll
    static void setup() {
        order = new Order(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                OrderStatus.READY,
                new Date(),
                new Date(),
                List.of()
        );
    }

    @DisplayName("Create order, expect status Created")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenHeadChefRole_whenCreateOrder_thenStatusCreated() {
        when(orderService.createOrder(any(Order.class), anyBoolean())).thenReturn(Mono.just(order));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/orders")
                .body(Mono.just(order), Order.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(System.out::println);

        verify(orderService, times(1)).createOrder(any(Order.class), anyBoolean());
    }

    @DisplayName("Get all orders, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenOrders_whenGetAllOrders_thenStatusOk() {
        when(orderService.getAllOrders(any())).thenReturn(Flux.just(order));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);

        verify(orderService, times(1)).getAllOrders(any());
    }

    @DisplayName("Get all delivered orders, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenOrderStatusDelivered_whenGetAllOrders_thenStatusOk() {
        when(orderService.getAllOrders(any(OrderStatus.class))).thenReturn(Flux.just(order));

        webTestClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/orders")
                                .queryParam("status", 3)
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);

        verify(orderService, times(1)).getAllOrders(any(OrderStatus.class));
    }

    @DisplayName("Get my orders with role delivery driver, expect status Ok")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void givenOrdersAndRoleDeliveryDriver_whenGetMyOrders_thenStatusOk() {
        when(orderService.getMyOrders(any())).thenReturn(Flux.just(order));

        webTestClient.get()
                .uri("/orders/my-orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].supplierId").isEqualTo("63d1b3dae8b8e7e8b68300af")
                .consumeWith(System.out::println);

        verify(orderService, times(1)).getMyOrders(any());
    }

    @DisplayName("Get my orders with role chef, expect status Forbidden")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenOrdersAndRoleChef_whenGetMyOrders_thenStatusForbidden() {
        webTestClient.get()
                .uri("/orders/my-orders")
                .exchange()
                .expectStatus().isForbidden();

        verify(orderService, never()).getMyOrders(any());
    }

    @DisplayName("Get order by id, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenOrders_whenGetOrderById_thenStatusOk() {
        when(orderService.getOrderById(anyString())).thenReturn(Mono.just(order));

        webTestClient.get()
                .uri("/orders/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);

        verify(orderService, times(1)).getOrderById(anyString());
    }

    @DisplayName("Approve order, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenReadyOrder_whenApproveOrder_thenStatusOk() {
        when(orderService.approveOrder(anyString())).thenReturn(Mono.just(order));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .put()
                .uri("/orders/123/approve")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);

        verify(orderService, times(1)).approveOrder(anyString());
    }

    @DisplayName("Reject order, expect status No Content")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenReadyOrder_WhenRejectOrder_thenStatusNoContent() {
        when(orderService.rejectOrder(anyString())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .delete()
                .uri("/orders/123/reject")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(System.out::println);

        verify(orderService, times(1)).rejectOrder(anyString());
    }

}
