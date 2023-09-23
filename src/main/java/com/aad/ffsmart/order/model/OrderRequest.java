package com.aad.ffsmart.order.model;

import com.aad.ffsmart.inventory.InventoryItem;
import com.aad.ffsmart.order.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderRequest {

    private String id;

    private String supplierId;

    private String supplierName;

    private String driverId;

    private OrderStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date placedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date deliveryDate;

    private List<InventoryItem> items;
}
