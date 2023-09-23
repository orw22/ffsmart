package com.aad.ffsmart.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Reactive Response handler class
 * <p>
 * Takes message, status and data and produces Spring response entity object
 * Method is overloaded for no data, Mono and Flux
 * <a href="https://spring.io/blog/2016/04/19/understanding-reactive-types">...</a>
 *
 * @author Oliver Wortley
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseHandler {
    private static final String MESSAGE = "message";
    private static final String DATA = "data";

    public static Mono<ResponseEntity<Object>> generateResponse(String message, HttpStatus status) {
        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE, message);
        map.put(DATA, null);

        return Mono.just(new ResponseEntity<>(map, status));
    }

    public static Mono<ResponseEntity<Object>> generateResponse(String message, HttpStatus status, Mono<?> data) {
        return data.flatMap(obj -> {
            Map<String, Object> map = Map.of(MESSAGE, message, DATA, obj);
            return Mono.just(new ResponseEntity<>(map, status));
        });
    }

    public static Mono<ResponseEntity<Object>> generateResponse(String message, HttpStatus status, Flux<?> data) {
        return data.collectList().flatMap(obj -> {
            Map<String, Object> map = Map.of(MESSAGE, message, DATA, obj);
            return Mono.just(new ResponseEntity<>(map, status));
        });
    }

}
