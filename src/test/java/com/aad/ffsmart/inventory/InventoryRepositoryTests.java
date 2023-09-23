package com.aad.ffsmart.inventory;

import com.aad.ffsmart.db.MongoConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ContextConfiguration(classes = MongoConfig.class)
@ExtendWith(SpringExtension.class)
class InventoryRepositoryTests {
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void givenInventory_whenFindAll_thenInventoryReturned() throws ParseException {
        Date from = formatter.parse("2023-01-08");
        Date to = formatter.parse("2023-02-05");
        Flux<InventoryItem> inventoryItemFlux = inventoryRepository.findAll(
                "banana", 10, 110, from, to
        );

        StepVerifier
                .create(inventoryItemFlux)
                .assertNext(item -> {
                    assertNotNull(item.getId());
                    assertTrue(item.getExpiryDate().after(from) && item.getExpiryDate().before(to));
                    assertTrue(item.getQuantity() >= 10 && item.getQuantity() <= 110);
                    assertEquals("Bananas 100g", item.getItemName());
                });
    }

    @Test
    void givenInventory_whenFindByItemIdExpiryDate_thenInventoryReturned() throws ParseException {
        Date date = formatter.parse("2023-03-14");
        String itemId = "0";

        Mono<InventoryItem> inventoryItemMono = inventoryRepository.findByItemIdExpiryDate(itemId, date);
        StepVerifier
                .create(inventoryItemMono)
                .assertNext(item -> {
                    assertEquals("0", item.getItemId());
                    assertEquals(date, item.getExpiryDate());
                })
                .verifyComplete();
    }

    @Test
    void givenExpiredInventory_whenFindExpired_thenExpiredItemsReturned() throws ParseException {
        inventoryRepository.save(new InventoryItem(
                null, "6", "Milk 2", "63d1b3dae8b8e7e8b68300af", "Supplier 1", 100, formatter.parse("2022-02-02")
        )).block();
        Flux<InventoryItem> inventoryItemMono = inventoryRepository.findExpired();

        StepVerifier
                .create(inventoryItemMono)
                .assertNext(item -> assertTrue(item.getExpiryDate().before(new Date())))
                .thenCancel()
                .verify();
    }

    @Test
    void givenExpiredInventory_whenDeleteExpired_thenNoExpiredItemsReturned() {
        inventoryRepository.deleteExpired().block();
        Flux<InventoryItem> inventoryItemFlux = inventoryRepository.findExpired();

        StepVerifier
                .create(inventoryItemFlux)
                .expectComplete()
                .verify(); // check no expired items
    }
}
