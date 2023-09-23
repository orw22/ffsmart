package com.aad.ffsmart.alert;

import reactor.core.publisher.Flux;

/**
 * Alert service interface
 *
 * Defines method signatures, implemented in AlertServiceImpl
 *
 */
public interface AlertService {

    Flux<Alert> getAlerts();
}
