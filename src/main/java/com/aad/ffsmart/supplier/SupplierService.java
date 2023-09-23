package com.aad.ffsmart.supplier;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * SupplierService interface
 * <p>
 * Contains method signatures for supplier logic
 *
 * @author Oliver Wortley
 */
public interface SupplierService {

    Mono<Supplier> getSupplierById(String supplierId);

    Flux<Supplier> getAllSuppliers(String supplierName);
}
