package com.aad.ffsmart.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * SupplierItems class
 *
 * Used for projection of inventory aggregations
 */
@Data
@AllArgsConstructor
public class SupplierItems {
    private String supplierId;

    private String supplierName;

    private List<ItemCount> items;
}
