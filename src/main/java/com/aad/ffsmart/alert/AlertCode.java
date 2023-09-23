package com.aad.ffsmart.alert;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Alert code enum
 * <p>
 * Used to identify what type of alert has been sent
 *
 * @author Oliver Wortley
 */
public enum AlertCode {
    ITEMS_TO_EXPIRE(0),
    LOW_QUANTITY_ITEMS(1),
    ORDER_READY(2),
    ORDER_PLACED(3),
    ORDER_DELIVERED(4),
    CHECKING_FUNCTION_RESULT(5);

    public final int value;

    private AlertCode(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
