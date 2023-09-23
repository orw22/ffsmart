package com.aad.ffsmart.supplier;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Supplier repository
 *
 * Maps to Suppliers collection in MongoDB Atlas
 * No extra methods need to be defined here
 *
 * @author Oliver Wortley
 */
@Repository
public interface SupplierRepository extends ReactiveMongoRepository<Supplier, String> {
}
