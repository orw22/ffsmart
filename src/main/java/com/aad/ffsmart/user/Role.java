package com.aad.ffsmart.user;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

/**
 * User role enum
 * <p>
 * Used for method authorization (e.g. delivery drivers cannot remove items from fridge)
 * Implements Spring GrantedAuthority (required for method auth to work)
 * <p>
 * ROLE_DELIVERY_DRIVER -> delivery driver
 * ROLE_CHEF -> chef
 * ROLE_HEAD_CHEF -> head chef
 *
 * @author Oliver Wortley
 */
public enum Role implements GrantedAuthority {
    ROLE_DELIVERY_DRIVER(0),
    ROLE_CHEF(1),
    ROLE_HEAD_CHEF(2);

    public final int value;

    private Role(int value) {
        this.value = value;
    }

    @Override
    public String getAuthority() {
        return name();
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
