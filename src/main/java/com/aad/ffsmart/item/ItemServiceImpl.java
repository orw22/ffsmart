package com.aad.ffsmart.item;

import com.aad.ffsmart.item.data.Items;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Items service implementation
 * <p>
 * Defines get all items method:
 * - nullable param name
 * - filters items so that item.name starts with name
 *
 * @author Oliver Wortley
 */
@Service
public class ItemServiceImpl implements ItemService {

    public Flux<Item> getAllItems(String name, String supplierId) {
        return Mono.just(
                Items.ITEM_LIST
                        .stream()
                        .filter(item -> name == null || item.getName().toLowerCase().startsWith(name.toLowerCase()))
                        .filter(item -> supplierId == null || item.getSupplierId().equals(supplierId))
                        .toList()
        ).flatMapMany(Flux::fromIterable);
    }
}
