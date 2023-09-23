package com.aad.ffsmart.supplier;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTests {
    private static Supplier testSupplier;
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @BeforeAll
    static void setup() {
        testSupplier = new Supplier(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                List.of(),
                "supplier1@gmail.com",
                "07577123123");
    }

    @DisplayName("Get all suppliers with no supplier name, expect suppliers returned")
    @Test
    void givenNoSupplierName_whenGetAllSuppliers_thenSuccess() {
        when(supplierRepository.findAll()).thenReturn(Flux.just(testSupplier));
        Flux<Supplier> supplierFlux = supplierService.getAllSuppliers(null);

        StepVerifier
                .create(supplierFlux)
                .consumeNextWith(supplier -> assertEquals(testSupplier, supplier))
                .verifyComplete();
    }

    @DisplayName("Get all suppliers with supplier name, expect suppliers returned")
    @Test
    void givenSupplierName_whenGetAllSuppliers_thenSuccess() {
        when(supplierRepository.findAll()).thenReturn(Flux.just(testSupplier));
        Flux<Supplier> supplierFlux = supplierService.getAllSuppliers("Supplier 1");

        StepVerifier
                .create(supplierFlux)
                .consumeNextWith(supplier -> assertEquals(testSupplier, supplier))
                .verifyComplete();
    }

    @DisplayName("Get supplier by id, expect supplier returned")
    @Test
    void givenSupplier_whenGetSupplierById_thenSuccess() {
        when(supplierRepository.findById(anyString())).thenReturn(Mono.just(testSupplier));

        Mono<Supplier> supplierMono = supplierService.getSupplierById("123");

        StepVerifier
                .create(supplierMono)
                .assertNext(supplier -> assertEquals(testSupplier, supplier))
                .verifyComplete();
    }

    @DisplayName("Get supplier with invalid id, expect supplier not found error")
    @Test
    void givenInvalidSupplierId_whenGetSupplierById_thenError() {
        when(supplierRepository.findById(anyString())).thenReturn(Mono.empty());
        Mono<Supplier> supplierMono = supplierService.getSupplierById("123");

        StepVerifier
                .create(supplierMono)
                .expectError()
                .verify();
    }

}
