package com.aad.ffsmart.alert;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AlertServiceTests {
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private AlertServiceImpl alertService;

    @DisplayName("Get alerts, expect flux of alerts returned")
    @Test
    void givenAlerts_whenGetAlerts_thenAlertsReturned() {
        final boolean[] i = {true};
        Alert alert = new Alert(AlertCode.ORDER_READY, "Order ready", "Order is ready for approval", new Date());
        when(rabbitTemplate.receiveAndConvert(anyString())).then(ans -> {
            if (i[0]) {
                i[0] = false;
                return alert;
            } else {
                return null;
            }
        });

        Flux<Alert> alertFlux = alertService.getAlerts();

        StepVerifier
                .create(alertFlux)
                .consumeNextWith(al -> {
                    log.debug(al.toString());
                    assertEquals(alert, al);
                })
                .verifyComplete();
    }

}
