package com.aad.ffsmart.user;

import com.aad.ffsmart.user.model.LoginForm;
import com.aad.ffsmart.user.model.TokenUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * User service interface
 * <p>
 * Defines method signatures for user service logic
 *
 * @author Oliver Wortley
 */
public interface UserService {
    Mono<TokenUser> login(LoginForm loginForm);

    Mono<TokenUser> register(User user);

    Flux<User> getAllUsers();

    Mono<User> getUserById(String userId);

    Mono<String> getUserNameFromId(String userId);

    Mono<User> updateUserById(String userId, User user);

    Mono<Void> deleteUserById(String userId);

}
