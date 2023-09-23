package com.aad.ffsmart.order;

import com.aad.ffsmart.inventory.InventoryItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

/**
 * Order data class
 * <p>
 * Defines schema for Orders collection
 * Has required args constructor for creating orders server-side (auto-generation)
 *
 * @author Oliver Wortley
 */
@Document("Orders")
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Order {

    @MongoId
    private String id;

    @NonNull
    private String supplierId;

    @NonNull
    private String supplierName;

    @Nullable
    private String driverId;

    @NonNull
    private OrderStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NonNull
    private Date placedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NonNull
    private Date deliveryDate;

    @NonNull
    private List<InventoryItem> items;

}
