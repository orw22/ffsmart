package com.aad.ffsmart.inventory;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.exception.GlobalExceptionHandler;
import com.aad.ffsmart.inventory.model.InventoryOperation;
import com.aad.ffsmart.web.ResponseMessage;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(InventoryController.class)
@Import(WebFluxTestSecurityConfig.class)
class InventoryControllerTests {
    private static InventoryChange inventoryChange;
    private static InventoryItem inventoryItem;

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private ServerWebExchange serverWebExchange;

    @BeforeAll
    static void setup() {
        inventoryItem = new InventoryItem("0", "Bananas 100g", "63d1b3dae8b8e7e8b68300af", "Supplier 1", 5, new Date());
        inventoryChange = new InventoryChange("123", "789", List.of(inventoryItem), InventoryOperation.INSERT, new Date());
    }

    @DisplayName("Add inventory, expect status Created")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void whenAddInventory_thenStatusCreated() {
        when(inventoryService.addInventory(anyList(), any())).thenReturn(Mono.just(inventoryChange));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/inventory/insert")
                .body(Mono.just(List.of(inventoryItem)), List.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .jsonPath("$.data.id").isEqualTo("123")
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).addInventory(anyList(), any());
    }

    @DisplayName("Remove inventory, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenInventory_WhenRemoveInventory_thenStatusOk() {
        when(inventoryService.removeInventory(anyList(), any())).thenReturn(Mono.just(inventoryChange));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/inventory/remove")
                .body(Mono.just(List.of(inventoryItem)), List.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .jsonPath("$.data.id").isEqualTo("123")
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).removeInventory(anyList(), any());
    }

    @DisplayName("Get all inventory, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenInventory_WhenGetAllInventory_thenStatusOk() {
        when(inventoryService.getAllInventory(anyString(), anyInt(), anyInt(), any(Date.class), any(Date.class))).thenReturn(Flux.just(inventoryItem));

        webTestClient.get()
                .uri("/inventory")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .jsonPath("$.data[0].itemId").isEqualTo("0")
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).getAllInventory(anyString(), anyInt(), anyInt(), any(Date.class), any(Date.class));
    }

    @DisplayName("Get inventory by id, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenInventory_WhenGetInventoryById_thenStatusOk() {
        when(inventoryService.getInventoryById(anyString())).thenReturn(Mono.just(inventoryItem));

        webTestClient.get()
                .uri("/inventory/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .jsonPath("$.data.itemId").isEqualTo("0")
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).getInventoryById(anyString());
    }

    @DisplayName("Update inventory by id, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenInventory_WhenUpdateInventoryById_thenStatusOk() {
        when(inventoryService.updateInventoryById(anyString(), any(InventoryItem.class))).thenReturn(Mono.just(inventoryItem));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .put()
                .uri("/inventory/123")
                .body(Mono.just(inventoryItem), InventoryItem.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).updateInventoryById(anyString(), any(InventoryItem.class));
    }

    @DisplayName("Get inventory change history, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenInventoryChanges_WhenGetInventoryChangeHistory_thenStatusOk() {
        when(inventoryService.getInventoryChangeHistory()).thenReturn(Flux.just(inventoryChange));

        webTestClient.get()
                .uri("/inventory/change-history")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).getInventoryChangeHistory();
    }

    @DisplayName("Get expired items, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenExpiredItems_WhenGetExpiredItems_thenStatusOk() {
        when(inventoryService.getExpiredItems()).thenReturn(Flux.just(inventoryItem));

        webTestClient.get()
                .uri("/inventory/expired-items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);

        verify(inventoryService, times(1)).getExpiredItems();
    }

    @DisplayName("Remove expired items, expect status No Content")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenExpiredItems_WhenRemoveExpiredItems_thenStatusNoContent() {
        when(inventoryService.removeExpiredItems()).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .delete()
                .uri("/inventory/expired-items")
                .exchange()
                .expectStatus().isNoContent();

        verify(inventoryService, times(1)).removeExpiredItems();
    }

}
