package com.aad.ffsmart.item;

import com.aad.ffsmart.web.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.aad.ffsmart.web.ResponseHandler.generateResponse;

/**
 * Items controller
 * <p>
 * Defines /items endpoints (get items)
 *
 * @author Oliver Wortley
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public Mono<ResponseEntity<Object>> getAllItems(@RequestParam(required = false) String name, @RequestParam(required = false) String supplierId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, itemService.getAllItems(name, supplierId));
    }

}
