package com.aad.ffsmart.inventory.model;

import lombok.Data;

/**
 * Part of SupplierItems class
 *
 * Used for inventory aggregation projection
 */
@Data
public class ItemCount {
    private String itemId;

    private String itemName;

    private Integer quantity;
}
