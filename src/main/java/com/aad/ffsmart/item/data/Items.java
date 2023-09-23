package com.aad.ffsmart.item.data;

import com.aad.ffsmart.item.Item;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Items data
 * <p>
 * Contains static list of items and their respective supplier IDs
 *
 * @author Oliver Wortley
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Items {
    private static final String SUPPLIER_1_ID = "63d1b3dae8b8e7e8b68300af";
    private static final String SUPPLIER_1_NAME = "Supplier 1";
    private static final String SUPPLIER_2_ID = "63d1b48ce8b8e7e8b68300b1";
    private static final String SUPPLIER_2_NAME = "Supplier 2";

    public static final List<Item> ITEM_LIST = List.of(
            new Item("0", "Bananas 100g", SUPPLIER_1_ID, SUPPLIER_1_NAME),
            new Item("1", "Apples 200g", SUPPLIER_2_ID, SUPPLIER_2_NAME),
            new Item("2", "Lamb 500g", SUPPLIER_1_ID, SUPPLIER_1_NAME),
            new Item("3", "Chicken 1kg", SUPPLIER_2_ID, SUPPLIER_2_NAME),
            new Item("5", "Broccoli 200g", SUPPLIER_2_ID, SUPPLIER_2_NAME),
            new Item("6", "Milk 2L", SUPPLIER_1_ID, SUPPLIER_1_NAME),
            new Item("7", "Coca Cola 1L", SUPPLIER_1_ID, SUPPLIER_2_NAME)
    );
}
