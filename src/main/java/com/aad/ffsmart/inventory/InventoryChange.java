package com.aad.ffsmart.inventory;

import com.aad.ffsmart.inventory.model.InventoryOperation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.List;

/**
 * Inventory change data class
 *
 * Defines schema for InventoryChanges collection
 *
 * @author Oliver Wortley
 *
 */
@Document("InventoryChanges")
@AllArgsConstructor
@Data
public class InventoryChange {

    @MongoId
    private String id;

    private String userId;

    private List<InventoryItem> items;

    private InventoryOperation operation; // 0 -> remove, 1 -> add

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;
}
