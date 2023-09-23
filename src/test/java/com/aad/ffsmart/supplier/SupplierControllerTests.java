package com.aad.ffsmart.supplier;

import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.exception.GlobalExceptionHandler;
import com.aad.ffsmart.web.WebFluxTestSecurityConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(SupplierController.class)
@Import(WebFluxTestSecurityConfig.class)
class SupplierControllerTests {

    private static Supplier testSupplier;
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private SupplierService supplierService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeAll
    public static void setup() {
        testSupplier = new Supplier(
                "63d1b3dae8b8e7e8b68300af",
                "Supplier 1",
                List.of(),
                "supplier1@gmail.com",
                "07577123123");
    }

    @DisplayName("Get all suppliers, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenSuppliers_whenGetAllSuppliers_thenStatusOk() {
        when(supplierService.getAllSuppliers(isNull())).thenReturn(Flux.just(testSupplier));

        webTestClient.get()
                .uri("/suppliers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
        verify(supplierService, times(1)).getAllSuppliers(isNull());
    }

    @DisplayName("Get supplier by id, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenSuppliers_whenGetSupplierById_thenStatusOk() {
        when(supplierService.getSupplierById(anyString())).thenReturn(Mono.just(testSupplier));

        webTestClient.get()
                .uri("/suppliers/63d1b3dae8b8e7e8b68300af")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
        verify(supplierService, times(1)).getSupplierById(anyString());
    }

}
