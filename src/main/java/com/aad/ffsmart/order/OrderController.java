package com.aad.ffsmart.order;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.order.model.OrderRequest;
import com.aad.ffsmart.web.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.aad.ffsmart.web.ResponseHandler.generateResponse;

/**
 * Order controller class
 * <p>
 * Defines /orders endpoints, response codes and role security, calls relevant service methods
 *
 * @author Oliver Wortley
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = new Order(
                orderRequest.getId(),
                orderRequest.getSupplierId(),
                orderRequest.getSupplierName(),
                orderRequest.getDriverId(),
                orderRequest.getStatus(),
                orderRequest.getPlacedDate(),
                orderRequest.getDeliveryDate(),
                orderRequest.getItems()
        );
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.CREATED, orderService.createOrder(order, false));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('DELIVERY_DRIVER') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getOrderById(@PathVariable String orderId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.getOrderById(orderId));
    }

    @GetMapping
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getAllOrders(@RequestParam(required = false) Integer status) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.getAllOrders(status != null ? OrderStatus.fromValue(status) : null));
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('DELIVERY_DRIVER')")
    public Mono<ResponseEntity<Object>> getApprovedOrders() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.getApprovedOrders());
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('DELIVERY_DRIVER')")
    public Mono<ResponseEntity<Object>> getMyOrders(ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.getMyOrders(userId));
    }

    @GetMapping("/ready")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getReadyOrders() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.getReadyOrders());
    }

    @PutMapping("/{orderId}/approve")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> approveOrder(@PathVariable String orderId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.approveOrder(orderId));
    }

    @DeleteMapping("/{orderId}/reject")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> rejectOrder(@PathVariable String orderId) {
        return orderService.rejectOrder(orderId).then(generateResponse("Order rejected", HttpStatus.NO_CONTENT));
    }

    @PutMapping("/{orderId}/in-transit")
    @PreAuthorize("hasRole('DELIVERY_DRIVER')")
    public Mono<ResponseEntity<Object>> markOrderInTransit(@PathVariable String orderId, ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.dispatchOrder(orderId, userId));
    }

    @PutMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('DELIVERY_DRIVER')")
    public Mono<ResponseEntity<Object>> deliverOrder(@PathVariable String orderId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, orderService.deliverOrder(orderId));
    }

}
