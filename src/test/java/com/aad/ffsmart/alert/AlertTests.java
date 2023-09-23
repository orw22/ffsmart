package com.aad.ffsmart.alert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AlertTests {
    @Autowired
    private AlertService alertService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    WebTestClient webTestClient;

    @DisplayName("Get alerts integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenAlerts_whenTestGetAlerts_thenStatusOkAndAlertsReturned() {
        rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                new Alert(AlertCode.ORDER_READY, "New order generated", "A new order has been generated and is ready for approval", new Date()));

        webTestClient.get()
                .uri("/alerts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].alertCode").isEqualTo(AlertCode.ORDER_READY.value)
                .consumeWith(System.out::println);
    }

}
