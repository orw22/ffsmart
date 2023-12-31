package com.aad.ffsmart.order;

import com.aad.ffsmart.alert.Alert;
import com.aad.ffsmart.alert.AlertCode;
import com.aad.ffsmart.alert.RabbitConfig;
import com.aad.ffsmart.inventory.InventoryChange;
import com.aad.ffsmart.inventory.InventoryItem;
import com.aad.ffsmart.inventory.InventoryService;
import com.aad.ffsmart.inventory.model.InventoryOperation;
import com.aad.ffsmart.inventory.model.SupplierItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Order service implementation
 * <p>
 * Contains order business logic inc. checking function and auto order generation
 *
 * @author Oliver Wortley
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static final String ORDER_NOT_FOUND_MSG = "Order not found";
    private static final String ORDER_STR = "Order ";
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private boolean checkOrder() {
//        checking function -> fails for 1 in 10 orders
        return Math.random() >= 0.1d;
    }

    @Scheduled(cron = "0 0 9 * * MON") // every MON at 9am
    private void autoGenerateOrder() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 4); // + 4 days
        Date deliveryDate = cal.getTime();
        cal.add(Calendar.DATE, 10);
        Date desiredExpiryDate = cal.getTime(); // + 14 days
        Map<String, Integer> weeklyRemovedQuantities = new HashMap<>();
        final Integer weeks = 4;

        List<InventoryChange> inventoryChanges = inventoryService.getInventoryChanges4Weeks().collectList().block();
        if (inventoryChanges != null) {
            inventoryChanges.forEach(change -> {
                if (change.getOperation() == InventoryOperation.REMOVE) {
                    change.getItems().forEach(item -> weeklyRemovedQuantities.merge(item.getItemId(), item.getQuantity(), Integer::sum));
                }
            });
        }
        weeklyRemovedQuantities.replaceAll((k, v) -> v / weeks); // compute average removed count

        List<SupplierItems> supplierItemsList = inventoryService.aggregateInventory().collectList().block();
        assert supplierItemsList != null;

        supplierItemsList.forEach(supplierItems ->
                createOrder(new Order(supplierItems.getSupplierId(), supplierItems.getSupplierName(), OrderStatus.READY, new Date(), deliveryDate,
                        supplierItems.getItems()
                                .stream()
                                .filter(it -> it.getQuantity() < weeklyRemovedQuantities.getOrDefault(it.getItemId(), 0))
                                .map(it -> new InventoryItem(
                                        it.getItemId(),
                                        it.getItemName(),
                                        supplierItems.getSupplierId(),
                                        supplierItems.getSupplierName(),
                                        weeklyRemovedQuantities.getOrDefault(it.getItemId(), 0) > 10
                                                ? weeklyRemovedQuantities.get(it.getItemId())
                                                : 10,
                                        desiredExpiryDate)
                                ).toList()
                ), true).block()
        );
    }

    public Mono<Order> createOrder(Order order, boolean autoGenerated) {
        if (!autoGenerated) {
            order.setStatus(OrderStatus.APPROVED);
        }
        log.info("Order being saved into database");
        log.info(order.toString());

        return orderRepository.save(order).map(odr -> {
            if (autoGenerated) {
                rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                        new Alert(AlertCode.ORDER_READY, "New order generated", "A new order has been generated and is ready for approval", new Date()));
            } else {
                rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                        new Alert(AlertCode.ORDER_PLACED, "Order placed", ORDER_STR + odr.getId() + " was approved and sent!", new Date()));
            }
            return odr;
        });
    }

    public Flux<Order> getAllOrders(OrderStatus status) {
        return status == null
                ? orderRepository.findAll().sort(Comparator.comparing(Order::getPlacedDate).reversed())
                : orderRepository.findAll(status).sort(Comparator.comparing(Order::getPlacedDate).reversed());
    }

    public Flux<Order> getMyOrders(String userId) {
        return orderRepository.findByDriverId(userId);
    }

    public Flux<Order> getReadyOrders() {
        return orderRepository.findReady();
    }

    public Flux<Order> getApprovedOrders() {
        return orderRepository.findApproved();
    }

    public Mono<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND_MSG)));
    }

    public Mono<Order> approveOrder(String orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND_MSG)))
                .map(order -> {
                    order.setStatus(OrderStatus.APPROVED);
                    return order;
                })
                .flatMap(order -> {
                    rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                            new Alert(AlertCode.ORDER_PLACED, "Order placed", ORDER_STR + orderId + " was approved and sent!", new Date()));
                    return orderRepository.save(order);
                });
    }

    public Mono<Void> rejectOrder(String orderId) {
        return orderRepository
                .findById(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND_MSG)))
                .flatMap(orderRepository::delete);
    }

    public Mono<Order> dispatchOrder(String orderId, String userId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND_MSG)))
                .map(order -> {
                    order.setStatus(OrderStatus.IN_TRANSIT);
                    order.setDriverId(userId);
                    return order;
                })
                .flatMap(orderRepository::save);
    }

    private Mono<Order> checkOrderAndAddInventory(Order order) {
        rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                new Alert(AlertCode.ORDER_DELIVERED, "Order delivered", ORDER_STR + order.getId() + " was just delivered!", new Date()));

        if (checkOrder()) {
            rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                    new Alert(AlertCode.CHECKING_FUNCTION_RESULT, "Checking function passed", "Checking function passed for order " + order.getId(), new Date()));
            inventoryService.addInventory(order.getItems(), order.getDriverId()).block();
        } else {
            rabbitTemplate.convertAndSend(RabbitConfig.ALERTS_EXCHANGE_NAME, RabbitConfig.HEAD_CHEF_QUEUE_NAME,
                    new Alert(AlertCode.CHECKING_FUNCTION_RESULT, "Checking function failed", "Checking function failed for order " + order.getId(), new Date()));
        }

        return Mono.just(order);
    }

    public Mono<Order> deliverOrder(String orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND_MSG)))
                .map(order -> {
                    order.setStatus(OrderStatus.DELIVERED);
                    return order;
                })
                .flatMap(orderRepository::save)
                .flatMap(this::checkOrderAndAddInventory);
    }

}
