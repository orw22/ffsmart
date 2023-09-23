package com.aad.ffsmart.supplier;

import com.aad.ffsmart.web.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.aad.ffsmart.web.ResponseHandler.generateResponse;

/**
 * Suppliers rest controller
 * <p>
 * Contains endpoints and mappings for /suppliers path:
 * - getSupplierById
 * - getAllSuppliers
 *
 * @author Oliver Wortley
 */
@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping("/{supplierId}")
    @PreAuthorize("hasRole('DELIVERY_DRIVER') or hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getSupplierById(@PathVariable String supplierId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, supplierService.getSupplierById(supplierId));
    }

    @GetMapping
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getAllSuppliers(@RequestParam(required = false) String supplierName) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, supplierService.getAllSuppliers(supplierName));
    }

}
