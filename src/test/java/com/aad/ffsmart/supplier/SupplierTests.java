package com.aad.ffsmart.supplier;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class SupplierTests {
    private static final String SUPPLIER_ID = "63d1b3dae8b8e7e8b68300af";
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private SupplierRepository supplierRepository;

    @DisplayName("Get all suppliers integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenSuppliers_whenGetAllSuppliers_thenStatusOkAndSuppliersReturned() {
        String json = webTestClient.get()
                .uri("/suppliers")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        Supplier[] res = JsonPath.parse(json).read("$.data", Supplier[].class);

        Mono<Supplier> supplierMono = supplierRepository.findById(SUPPLIER_ID);
        StepVerifier
                .create(supplierMono)
                .consumeNextWith(supplier -> {
                    assertEquals(supplier.getEmail(), res[0].getEmail());
                    assertEquals(supplier.getId(), res[0].getId());
                })
                .verifyComplete();
    }

    @DisplayName("Get supplier by id integration test")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenSuppliers_whenGetSupplierById_thenStatusOkAndSupplierReturned() {
        String json = webTestClient.get()
                .uri("/suppliers/" + SUPPLIER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(System.out::println)
                .returnResult()
                .getResponseBody();
        Supplier res = JsonPath.parse(json).read("$.data", Supplier.class);

        Mono<Supplier> supplierMono = supplierRepository.findById(SUPPLIER_ID);
        StepVerifier
                .create(supplierMono)
                .consumeNextWith(supplier -> {
                    assertEquals(supplier.getEmail(), res.getEmail());
                    assertEquals(supplier.getId(), res.getId());
                })
                .verifyComplete();
    }
}