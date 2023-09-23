package com.aad.ffsmart.user;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.user.model.LoginForm;
import com.aad.ffsmart.user.model.UserRequest;
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
 * User rest controller class
 * <p>
 * Contains endpoint definitions for /users base path
 *
 * @author Oliver Wortley
 */
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody LoginForm loginForm) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.login(loginForm));
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> register(@RequestBody UserRequest userRequest) {
        User user = new User(
                userRequest.getId(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getRole(),
                userRequest.getAvatar()
        );
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.CREATED, userService.register(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getAllUsers() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getUserById(@PathVariable String userId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.getUserById(userId));
    }

    @GetMapping("/{userId}/name")
    @PreAuthorize("hasRole('CHEF') or hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getUserNameFromId(@PathVariable String userId) {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.getUserNameFromId(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> updateUserById(@PathVariable String userId, @RequestBody UserRequest userRequest) {
        User user = new User(
                userRequest.getId(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getRole(),
                userRequest.getAvatar()
        );
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.updateUserById(userId, user));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> deleteUserById(@PathVariable String userId) {
        return userService.deleteUserById(userId).then(generateResponse(ResponseMessage.SUCCESS, HttpStatus.NO_CONTENT));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Object>> getUserMe(ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.getUserById(userId));
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<Object>> updateUserMe(@RequestBody UserRequest userRequest, ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        User user = new User(
                userRequest.getId(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getRole(),
                userRequest.getAvatar()
        );

        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, userService.updateUserById(userId, user));
    }

    @DeleteMapping("/me")
    public Mono<ResponseEntity<Object>> deleteUserMe(ServerWebExchange serverWebExchange) {
        String authToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(authToken != null ? authToken.substring(7) : null);
        return userService.deleteUserById(userId).then(generateResponse(ResponseMessage.SUCCESS, HttpStatus.NO_CONTENT));
    }

}
