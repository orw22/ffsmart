package com.aad.ffsmart.exception;

import lombok.Getter;

/**
 * Custom error attributes enum
 * <p>
 * For custom error handling
 * Defines status, message and timestamp as the three fields to be returned
 *
 * @author Oliver Wortley
 */
@Getter
public enum ErrorAttributesKey {
    STATUS("status"),
    MESSAGE("message"),
    TIME("timestamp");

    private final String key;

    ErrorAttributesKey(String key) {
        this.key = key;
    }
}