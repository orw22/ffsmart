package com.aad.ffsmart.inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Inventory changes repository
 *
 * All required methods predefined by Spring magic
 *
 * @author Oliver Wortley
 */
@Repository
public interface InventoryChangeRepository extends ReactiveMongoRepository<InventoryChange, String> {
}
