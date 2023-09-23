package com.aad.ffsmart.inventory;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.inventory.model.InventoryItemRequest;
import com.aad.ffsmart.web.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static com.aad.ffsmart.web.ResponseHandler.generateResponse;

/**
 * Inventory controller class
 * <p>
 * Defines endpoints for /inventory path with params, request body, required user role
 *
 * @author Oliver Wortley
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/insert")
    @PreAuthorize("hasRole('DELIVERY_DRIVER') or hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> addInventory(@RequestBody List<InventoryItem> items, ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.CREATED, inventoryService.addInventory(items, userId));
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> removeInventory(@RequestBody List<InventoryItem> items, ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.removeInventory(items, userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getAllInventory(@RequestParam(required = false, defaultValue = "") String itemName,
                                                        @RequestParam(required = false, defaultValue = "0") Integer minQuantity,
                                                        @RequestParam(required = false, defaultValue = "100000") Integer maxQuantity,
                                                        @RequestParam(required = false, defaultValue = "2020-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expiryDateFrom,
                                                        @RequestParam(required = false, defaultValue = "2030-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expiryDateTo) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.getAllInventory(itemName, minQuantity, maxQuantity, expiryDateFrom, expiryDateTo));
    }

    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getInventoryById(@PathVariable String inventoryId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.getInventoryById(inventoryId));
    }

    @PutMapping("/{inventoryId}")
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> updateInventoryById(@PathVariable String inventoryId, @RequestBody InventoryItemRequest inventoryItemRequest) {
        InventoryItem inventoryItem = new InventoryItem(
                inventoryItemRequest.getId(),
                inventoryItemRequest.getItemId(),
                inventoryItemRequest.getItemName(),
                inventoryItemRequest.getSupplierId(),
                inventoryItemRequest.getSupplierName(),
                inventoryItemRequest.getQuantity(),
                inventoryItemRequest.getExpiryDate()
        );
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.updateInventoryById(inventoryId, inventoryItem));
    }

    @GetMapping("/change-history")
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getInventoryChangeHistory() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.getInventoryChangeHistory());
    }

    @GetMapping("/change-history/{inventoryChangeId}")
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getInventoryChangeById(@PathVariable String inventoryChangeId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.getInventoryChangeById(inventoryChangeId));
    }

    @GetMapping("/expired-items")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getExpiredItems() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, inventoryService.getExpiredItems());
    }

    @DeleteMapping("/expired-items")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> removeExpiredItems() {
        return inventoryService.removeExpiredItems().then(generateResponse(ResponseMessage.SUCCESS, HttpStatus.NO_CONTENT));
    }

}
