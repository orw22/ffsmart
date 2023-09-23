package com.aad.ffsmart.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Item data class
 *
 * Used in Items
 *
 * @author Oliver Wortley
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    private String id;

    private String name;

    private String supplierId;

    private String supplierName;
}
