package com.aad.ffsmart.supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Supplier service implementation
 * <p>
 * Implements service interface, calls repository functions
 * Methods: get supplier by ID, get all suppliers
 *
 * @author Oliver Wortley
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public Mono<Supplier> getSupplierById(String supplierId) {
        return supplierRepository.findById(supplierId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found")));
    }

    public Flux<Supplier> getAllSuppliers(String supplierName) {
        if (supplierName != null) {
            return supplierRepository.findAll().filter(sp -> sp.getName().startsWith(supplierName));
        }
        return supplierRepository.findAll();
    }
}
