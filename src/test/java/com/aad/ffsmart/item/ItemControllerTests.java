package com.aad.ffsmart.item;

import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.exception.GlobalExceptionHandler;
import com.aad.ffsmart.item.data.Items;
import com.aad.ffsmart.web.WebFluxTestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
@Import(WebFluxTestSecurityConfig.class)
class ItemControllerTests {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @DisplayName("Get all items, expect status Ok")
    @WithMockUser
    @Test
    void givenItems_whenGetAllItems_thenStatusOk() {
        when(itemService.getAllItems(isNull(), isNull())).thenReturn(Mono.just(Items.ITEM_LIST).flatMapMany(Flux::fromIterable));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
        verify(itemService, times(1)).getAllItems(isNull(), isNull());
    }

}
