package com.aad.ffsmart.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

/**
 * InventoryItem data class
 * <p>
 * Features all args and required args constructors
 * Defines schema for Inventory collection
 *
 * @author Oliver Wortley
 */
@Document("Inventory")
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Data
public class InventoryItem {

    @MongoId
    private String id;

    @NonNull
    private String itemId;

    @NonNull
    private String itemName;

    @NonNull
    private String supplierId;

    @NonNull
    private String supplierName;

    @NonNull
    private Integer quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NonNull
    private Date expiryDate;
}
