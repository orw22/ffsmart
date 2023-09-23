package com.aad.ffsmart.inventory.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Inventory operation enum
 * <p>
 * Insert -> 1
 * Remove -> 0
 *
 * @author Oliver Wortley
 */
public enum InventoryOperation {

    INSERT(1),
    REMOVE(0);

    public final int value;

    private InventoryOperation(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
