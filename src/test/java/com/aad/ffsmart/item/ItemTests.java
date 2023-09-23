package com.aad.ffsmart.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class ItemTests {
    @Autowired
    private ItemService itemService;

    @Autowired
    WebTestClient webTestClient;

    @DisplayName("Get all items integration test")
    @WithMockUser
    @Test
    void givenItems_whenGetItems_thenStatusOkAndItemsReturned() {
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo("0")
                .jsonPath("$.data[0].name").isEqualTo("Bananas 100g")
                .jsonPath("$.data[3].supplierName").isEqualTo("Supplier 2")
                .consumeWith(System.out::println);
    }

}
