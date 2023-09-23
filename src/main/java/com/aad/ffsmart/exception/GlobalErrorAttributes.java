package com.aad.ffsmart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Override global error attributes
 * Processes ResponseStatusException and translates into error response
 *
 * @author Oliver Wortley
 */
@Component
@Slf4j
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);

        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String message = error instanceof ResponseStatusException ? error.getMessage().split("\"")[1] : error.getMessage();
        return Map.of(
                ErrorAttributesKey.STATUS.getKey(), determineHttpStatus(error).value(),
                ErrorAttributesKey.MESSAGE.getKey(), message,
                ErrorAttributesKey.TIME.getKey(), timestamp);
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        return error instanceof ResponseStatusException err
                ? HttpStatus.valueOf(err.getStatusCode().value())
                : MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ResponseStatus.class)
                .getValue(ErrorAttributesKey.STATUS.getKey(), HttpStatus.class)
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}