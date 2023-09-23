package com.aad.ffsmart.inventory;

import com.aad.ffsmart.alert.Alert;
import com.aad.ffsmart.alert.AlertCode;
import com.aad.ffsmart.alert.RabbitConfig;
import com.aad.ffsmart.inventory.model.InventoryOperation;
import com.aad.ffsmart.inventory.model.SupplierItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Inventory service implementation class
 * <p>
 * Contains all inventory logic, uses repository interface methods. Methods include:
 * - add inventory
 * - remove inventory
 * - get expired items
 * - remove expired items
 * - get inventory change history
 *
 * @author Oliver Wortley
 */
@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryChangeRepository inventoryChangeRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 0 12 * * *") // every day at 12pm
    private void alertWhenExpiringItems() {
        List<InventoryItem> items = inventoryRepository.findAll().collectList().block();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 3);

        assert items != null;
        if (!items.stream().filter(it -> it.getExpiryDate().before(calendar.getTime()) && it.getExpiryDate().after(new Date())).toList().isEmpty()) {
            rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                    new Alert(AlertCode.ITEMS_TO_EXPIRE, "Expiring items", "Some items in the fridge are due to expire within the next 3 days", new Date()));
        }
    }

    public Mono<InventoryChange> addInventory(List<InventoryItem> items, String userId) {
        return Mono.just(items).flatMapMany(Flux::fromIterable).flatMap(item -> inventoryRepository.findByItemIdExpiryDate(item.getItemId(), item.getExpiryDate())
                .map(it -> {
                    it.setQuantity(it.getQuantity() + item.getQuantity());
                    return it;
                })
                .flatMap(inventoryRepository::save)
                .switchIfEmpty(inventoryRepository.save(item))
        ).then(inventoryChangeRepository.save(new InventoryChange(
                null,
                userId,
                items,
                InventoryOperation.INSERT,
                new Date()
        )));
    }

    public Mono<InventoryChange> removeInventory(List<InventoryItem> items, String userId) {
        return Mono.just(items).flatMapMany(Flux::fromIterable).flatMap(item -> inventoryRepository.findByItemIdExpiryDate(item.getItemId(), item.getExpiryDate())
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found")))
                        .map(it -> {
                            it.setQuantity(it.getQuantity() - item.getQuantity());
                            return it;
                        })
                        .flatMap(it -> it.getQuantity() <= 0 ? inventoryRepository.deleteById(it.getId()) : inventoryRepository.save(it)))
                .then(inventoryChangeRepository.save(new InventoryChange(
                        null,
                        userId,
                        items,
                        InventoryOperation.REMOVE,
                        new Date()
                )));
    }

    public Flux<InventoryItem> getAllInventory(String itemName, Integer minQuantity, Integer maxQuantity, Date expiryDateFrom, Date expiryDateTo) {
        return inventoryRepository.findAll(itemName, minQuantity, maxQuantity, expiryDateFrom, expiryDateTo)
                .sort(Comparator.comparing(InventoryItem::getExpiryDate).reversed());
    }

    public Mono<InventoryItem> getInventoryById(String inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found")));
    }

    public Mono<InventoryItem> updateInventoryById(String inventoryId, InventoryItem inventoryItem) {
        return inventoryRepository.findById(inventoryId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found")))
                .then(inventoryRepository.save(inventoryItem));
    }

    public Flux<InventoryChange> getInventoryChangeHistory() {
        return inventoryChangeRepository.findAll().sort(Comparator.comparing(InventoryChange::getDate).reversed());
    }

    public Flux<InventoryChange> getInventoryChanges4Weeks() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -4);
        return inventoryChangeRepository.findAll()
                .filter(change -> change.getDate().after(calendar.getTime()))
                .sort(Comparator.comparing(InventoryChange::getDate).reversed());
    }

    public Mono<InventoryChange> getInventoryChangeById(String inventoryChangeId) {
        return inventoryChangeRepository.findById(inventoryChangeId);
    }

    public Flux<InventoryItem> getExpiredItems() {
        return inventoryRepository.findExpired();
    }

    public Mono<Void> removeExpiredItems() {
        return inventoryRepository.deleteExpired();
    }

    public Flux<SupplierItems> aggregateInventory() {
        return inventoryRepository.aggregateInventory();
    }

}
