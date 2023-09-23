package com.aad.ffsmart.item;

import reactor.core.publisher.Flux;

/**
 * Item service interface
 * <p>
 * Defines getAllItems method, implemented in ItemServiceImpl
 *
 * @author Oliver Wortley
 */
public interface ItemService {

    Flux<Item> getAllItems(String name, String supplierId);
}
