package com.aad.ffsmart.inventory;

import com.aad.ffsmart.inventory.model.SupplierItems;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * Inventory repository
 * <p>
 * Links to Inventory collection in MongoDB Atlas
 * Contains queries for finding all inventory, removing expired, and aggregation used in auto order generation
 *
 * @author Oliver Wortley
 */
@Repository
public interface InventoryRepository extends ReactiveMongoRepository<InventoryItem, String> {

    @Query(value = "{ itemName : { $regex : '^?0', $options : 'i' }, quantity : { $gte : ?1, $lte : ?2}, expiryDate : { $gte : ?3, $lte : ?4}}")
    Flux<InventoryItem> findAll(String itemName, Integer minQuantity, Integer maxQuantity, Date expiryDateFrom, Date expiryDateTo);

    @Query(value = "{ $and : [ { itemId : ?0}, { expiryDate : ?1 } ] }")
    Mono<InventoryItem> findByItemIdExpiryDate(String itemId, Date expiryDate);

    @Query(value = "{ expiryDate : { $lt : new Date() } }")
    Flux<InventoryItem> findExpired();

    @Query(value = "{ expiryDate : { $lt : new Date() } }", delete = true)
    Mono<Void> deleteExpired();

    @Aggregation(pipeline = {
            "{ $group : { _id : { supplierId : '$supplierId', itemId : '$itemId' }, supplierName: { $first : '$supplierName' }, itemName: { $first: '$itemName' }, total: { $sum : $quantity } } }",
            "{ $group : { _id :  '$_id.supplierId', supplierName: { $first : $supplierName }, items: { $push: { itemId : '$_id.itemId', itemName : '$itemName', quantity : $total } } } }",
            "{ $project : { _id : 0, supplierId : '$_id', supplierName: 1, items : '$items' } }"})
    Flux<SupplierItems> aggregateInventory();
}
