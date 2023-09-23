package com.aad.ffsmart.order;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Order repository
 *
 * Links to Orders collection in MongoDB
 *
 * Contains queries to filter orders by status APPROVED and status READY
 *
 * @author Oliver Wortley
 */
@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    @Query(value = "{ status : ?0 }")
    public Flux<Order> findAll(OrderStatus status);

    @Query(value = "{ driverId: ?0 }")
    public Flux<Order> findByDriverId(String driverId);

    @Query(value = "{status: APPROVED}")
    public Flux<Order> findApproved();

    @Query(value = "{status: READY}")
    public Flux<Order> findReady();
}
