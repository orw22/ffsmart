package com.aad.ffsmart.inventory;

import com.aad.ffsmart.inventory.model.InventoryOperation;
import com.aad.ffsmart.inventory.model.SupplierItems;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class InventoryServiceTests {
    private static InventoryItem inventoryItem;
    private static InventoryItem updatedInventoryItem;
    private static InventoryChange inventoryChange;
    private static InventoryChange inventoryChangeRemove;
    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryChangeRepository inventoryChangeRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @BeforeAll
    static void setup() {
        inventoryItem = new InventoryItem("0", "Bananas 100g", "63d1b3dae8b8e7e8b68300af", "Supplier 1", 5, new Date());
        updatedInventoryItem = new InventoryItem("0", "Bananas 100g", "63d1b3dae8b8e7e8b68300af", "Supplier 1", 20, new Date());
        inventoryChange = new InventoryChange("123", "789", List.of(inventoryItem), InventoryOperation.INSERT, new Date());
        inventoryChangeRemove = new InventoryChange("123", "789", List.of(inventoryItem), InventoryOperation.REMOVE, new Date());
    }

    @DisplayName("Add inventory, expect inventory added and inventory change returned")
    @Test
    void givenInventory_whenAddInventory_thenInventoryChangeReturned() {
        when(inventoryRepository.findByItemIdExpiryDate(anyString(), any(Date.class))).thenReturn(Mono.just(inventoryItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(Mono.just(inventoryItem));
        when(inventoryChangeRepository.save(any(InventoryChange.class))).thenReturn(Mono.just(inventoryChange));

        Mono<InventoryChange> inventoryChangeMono = inventoryService.addInventory(List.of(inventoryItem), "123");

        StepVerifier
                .create(inventoryChangeMono)
                .assertNext(change -> {
                    log.debug(change.toString());
                    assertEquals(inventoryChange, change);
                })
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByItemIdExpiryDate(anyString(), any(Date.class));
        verify(inventoryRepository, times(2)).save(any(InventoryItem.class));
        verify(inventoryChangeRepository, times(1)).save(any(InventoryChange.class));
    }

    @DisplayName("Remove inventory, expect inventory removed and inventory change returned")
    @Test
    void givenInventory_whenRemoveInventory_thenInventoryChangeReturned() {
        when(inventoryRepository.findByItemIdExpiryDate(anyString(), any(Date.class))).thenReturn(Mono.just(inventoryItem));
        when(inventoryRepository.deleteById((String) isNull())).thenReturn(Mono.empty());
        when(inventoryChangeRepository.save(any(InventoryChange.class))).thenReturn(Mono.just(inventoryChangeRemove));

        Mono<InventoryChange> inventoryChangeMono = inventoryService.removeInventory(List.of(inventoryItem), "789");

        StepVerifier
                .create(inventoryChangeMono)
                .assertNext(change -> {
                    log.debug(change.toString());
                    assertEquals(inventoryChangeRemove, change);
                })
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByItemIdExpiryDate(anyString(), any(Date.class));
        verify(inventoryChangeRepository, times(1)).save(any(InventoryChange.class));
    }

    @DisplayName("Get all inventory, expect inventory returned")
    @Test
    void givenInventory_whenGetAllInventory_thenSuccess() {
        when(inventoryRepository.findAll(any(), any(), any(), any(), any())).thenReturn(Flux.just(inventoryItem));
        Flux<InventoryItem> inventoryFlux = inventoryService
                .getAllInventory(null, null, null, null, null);

        StepVerifier
                .create(inventoryFlux)
                .consumeNextWith(item -> assertEquals(inventoryItem, item))
                .verifyComplete();

        verify(inventoryRepository, times(1)).findAll(any(), any(), any(), any(), any());
    }

    @DisplayName("Get inventory by id, expect inventory returned")
    @Test
    void givenInventory_whenGetInventoryById_thenSuccess() {
        when(inventoryRepository.findById(anyString())).thenReturn(Mono.just(inventoryItem));
        Mono<InventoryItem> inventoryMono = inventoryService.getInventoryById("123");

        StepVerifier
                .create(inventoryMono)
                .assertNext(item -> assertEquals(inventoryItem, item))
                .verifyComplete();

        verify(inventoryRepository, times(1)).findById(anyString());
    }

    @DisplayName("Update inventory by id, expect updated inventory returned")
    @Test
    void givenInventory_whenUpdateInventoryById_thenSuccess() {
        when(inventoryRepository.findById(anyString())).thenReturn(Mono.just(inventoryItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(Mono.just(updatedInventoryItem));
        Mono<InventoryItem> inventoryMono = inventoryService.updateInventoryById("123", updatedInventoryItem);

        StepVerifier
                .create(inventoryMono)
                .assertNext(item -> assertEquals(updatedInventoryItem, item))
                .verifyComplete();

        verify(inventoryRepository, times(1)).findById(anyString());
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
    }

    @DisplayName("Get inventory change history, expect successful")
    @Test
    void givenInventoryChange_whenGetInventoryChangeHistory_thenSuccess() {
        when(inventoryChangeRepository.findAll()).thenReturn(Flux.just(inventoryChange));
        Flux<InventoryChange> inventoryChangeFlux = inventoryService.getInventoryChangeHistory();

        StepVerifier
                .create(inventoryChangeFlux)
                .consumeNextWith(change -> assertEquals(inventoryChange, change))
                .verifyComplete();

        verify(inventoryChangeRepository, times(1)).findAll();
    }

    @DisplayName("Get inventory change by id, expect successful")
    @Test
    void givenInventoryChange_whenGetInventoryChangeById_thenSuccess() {
        when(inventoryChangeRepository.findById(anyString())).thenReturn(Mono.just(inventoryChange));
        Mono<InventoryChange> inventoryChangeFlux = inventoryService.getInventoryChangeById("123");

        StepVerifier
                .create(inventoryChangeFlux)
                .assertNext(change -> assertEquals(inventoryChange, change))
                .verifyComplete();

        verify(inventoryChangeRepository, times(1)).findById(anyString());
    }

    @DisplayName("Get expired items, expect expired items returned")
    @Test
    void givenExpiredItems_whenGetExpiredItems_thenSuccessful() {
        when(inventoryRepository.findExpired()).thenReturn(Flux.just(inventoryItem));
        Flux<InventoryItem> expiredItemFlux = inventoryService.getExpiredItems();

        StepVerifier
                .create(expiredItemFlux)
                .consumeNextWith(item -> assertEquals(inventoryItem, item))
                .verifyComplete();

        verify(inventoryRepository, times(1)).findExpired();
    }

    @DisplayName("Remove expired items, expect successful")
    @Test
    void givenExpiredItems_whenRemoveExpiredItems_thenSuccessful() {
        inventoryService.removeExpiredItems();
        verify(inventoryRepository, times(1)).deleteExpired();
    }

    @DisplayName("Aggregate inventory for order generation, expect grouped inventory returned")
    @Test
    void givenInventory_whenAggregateInventory_thenSuccessful() {
        SupplierItems aggregation = new SupplierItems("123", "ABC", List.of());
        when(inventoryRepository.aggregateInventory()).thenReturn(Flux.just(aggregation));

        Flux<SupplierItems> supplierItemsFlux = inventoryService.aggregateInventory();

        StepVerifier
                .create(supplierItemsFlux)
                .consumeNextWith(items -> {
                    log.debug(items.toString());
                    assertEquals(aggregation, items);
                })
                .verifyComplete();

        verify(inventoryRepository, times(1)).aggregateInventory();
    }

}
