package com.aad.ffsmart.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ItemServiceTests {
    @InjectMocks
    private ItemServiceImpl itemService;

    @DisplayName("Get all items, expect flux of all items returned")
    @Test
    void givenItems_whenGetAllItems_thenItemsReturned() {
        Flux<Item> itemFlux = itemService.getAllItems(null, null).take(1);

        StepVerifier
                .create(itemFlux)
                .consumeNextWith(item -> {
                    assertNotNull(item.getId());
                    assertEquals("0", item.getId());
                    assertEquals("Supplier 1", item.getSupplierName());
                })
                .verifyComplete();
    }

}