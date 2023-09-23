package com.aad.ffsmart.alert;

import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.web.WebFluxTestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Date;

import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@WebFluxTest(AlertController.class)
@Import(WebFluxTestSecurityConfig.class)
class AlertControllerTests {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private AlertService alertService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @DisplayName("Get alerts, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenAlerts_whenGetAlerts_thenStatusOk() {
        Alert alert = new Alert(AlertCode.ORDER_READY, "Order ready", "Order is ready for approval", new Date());
        when(alertService.getAlerts()).thenReturn(Flux.just(alert));

        webTestClient.get()
                .uri("/alerts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].alertCode").isEqualTo(AlertCode.ORDER_READY.value)
                .consumeWith(System.out::println);
        verify(alertService, times(1)).getAlerts();
    }

    @DisplayName("Get alerts with delivery driver role, expect status Forbidden")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void givenAlertsAndDeliveryDriverRole_whenGetAlerts_thenStatusForbidden() {
        webTestClient.get()
                .uri("/alerts")
                .exchange()
                .expectStatus().isForbidden();
        verify(alertService, never()).getAlerts();
    }

}
