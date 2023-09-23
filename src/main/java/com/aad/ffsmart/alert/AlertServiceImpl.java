package com.aad.ffsmart.alert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Alert service implementation class
 * <p>
 * Contains functionality for reading alerts from the head chef's queue and returning as Flux
 *
 * @author Oliver Wortley
 */
@Service
@Slf4j
public class AlertServiceImpl implements AlertService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Flux<Alert> getAlerts() {
        List<Alert> alerts = new ArrayList<>();
        Alert alert = (Alert) rabbitTemplate.receiveAndConvert(RabbitConfig.HEAD_CHEF_QUEUE_NAME);
        while (alert != null) {
            alerts.add(0, alert);
            alert = (Alert) rabbitTemplate.receiveAndConvert(RabbitConfig.HEAD_CHEF_QUEUE_NAME);
        }
        return Mono.just(alerts).flatMapMany(Flux::fromIterable);
    }
}
