package com.aad.ffsmart.inventory;

import com.aad.ffsmart.inventory.model.SupplierItems;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

/**
 * Inventory service
 * <p>
 * Defines methods for inventory business logic
 * Implemented in InventoryServiceImpl
 *
 * @author Oliver Wortley
 */
public interface InventoryService {

    Mono<InventoryChange> addInventory(List<InventoryItem> items, String userId);

    Mono<InventoryChange> removeInventory(List<InventoryItem> items, String userId);

    Flux<InventoryItem> getAllInventory(String itemName, Integer minQuantity, Integer maxQuantity, Date expiryDateFrom, Date expiryDateTo);

    Mono<InventoryItem> getInventoryById(String inventoryId);

    Mono<InventoryItem> updateInventoryById(String inventoryId, InventoryItem inventoryItem);

    Flux<InventoryChange> getInventoryChangeHistory();

    Flux<InventoryChange> getInventoryChanges4Weeks();

    Mono<InventoryChange> getInventoryChangeById(String inventoryChangeId);

    Flux<InventoryItem> getExpiredItems();

    Mono<Void> removeExpiredItems();

    Flux<SupplierItems> aggregateInventory();
}
