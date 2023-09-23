package com.aad.ffsmart.supplier;

import com.aad.ffsmart.item.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

/**
 * Supplier data class
 * <p>
 * Schema for Suppliers collection in DB
 *
 * @author Oliver Wortley
 */
@Document("Suppliers")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Supplier {

    @MongoId
    private String id;

    private String name;

    private List<Item> items;

    private String email;

    private String phone;

}
