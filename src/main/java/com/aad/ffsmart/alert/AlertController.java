package com.aad.ffsmart.alert;

import com.aad.ffsmart.web.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.aad.ffsmart.web.ResponseHandler.generateResponse;

/**
 * Alert controller class
 *
 * Allows head chef to get new alerts, older alerts are cached client-side
 *
 * @author Oliver Wortley
 *
 */
@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getAlerts() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, alertService.getAlerts());
    }
}
