package com.aad.ffsmart.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class InventoryItemRequest {

    private String id;

    private String itemId;

    private String itemName;

    private String supplierId;

    private String supplierName;

    private Integer quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date expiryDate;
}
