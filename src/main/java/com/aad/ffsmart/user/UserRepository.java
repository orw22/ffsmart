package com.aad.ffsmart.user;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * User repository
 *
 * Maps to Users collection in DB
 * Defines extra findByEmail method for login functionality
 *
 * @author Oliver Wortley
 */
@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    @Query(value = "{email : ?0}")
    Mono<User> findByEmail(String email);
}
